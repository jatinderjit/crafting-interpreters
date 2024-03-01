package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment = globals
    private val locals = HashMap<Expr, Int>()

    init {
        builtins()
    }

    private fun builtins() {
        globals.define("clock", object : LoxCallable {
            /**
             * Time in seconds
             */
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any =
                System.currentTimeMillis().toDouble() / 1000.0

            override fun arity(): Int = 0
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach(::execute)
        } catch (error: RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    private fun execute(stmt: Stmt) =
        stmt.accept(this)

    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val prevEnvironment = this.environment
        this.environment = environment
        try {
            statements.forEach(::execute)
        } finally {
            this.environment = prevEnvironment
        }
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    override fun visitBlockStmt(stmt: Stmt.Block) =
        executeBlock(stmt.statements, Environment(environment))

    override fun visitClassStmt(stmt: Stmt.Class) {
        val superclass = stmt.superclass?.let {
            val superclass = evaluate(it)
            if (superclass !is LoxClass) {
                throw RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
            superclass
        }

        environment.define(stmt.name.lexeme, null)

        val prevEnvironment = environment
        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = mutableMapOf<String, LoxFunction>()
        stmt.methods.forEach { method ->
            val isInitializer = method.name.lexeme == "init"
            val function = LoxFunction(method, environment, isInitializer)
            methods[method.name.lexeme] = function
        }
        val klass = LoxClass(stmt.name.lexeme, superclass, methods)

        environment = prevEnvironment
        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let(::execute)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = stmt.value?.let(::evaluate)
        throw Return(value)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        val value = stmt.initializer?.let(::evaluate)
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        locals[expr].let { distance ->
            if (distance != null) {
                environment.assignAt(distance, expr.name, value)
            } else {
                globals.assign(expr.name, value)
            }
        }
        return value
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            BANG_EQUAL -> return left != right
            EQUAL_EQUAL -> return left == right
            else -> {}
        }

        if (expr.operator.type == PLUS) {
            return when {
                left is String && right is String -> left + right
                left is Double && right is Double -> left + right
                else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
        }

        if (left !is Double || right !is Double) {
            throw RuntimeError(expr.operator, "Operands must be numbers.")
        }
        return when (expr.operator.type) {
            MINUS -> left - right
            SLASH -> left / right
            STAR -> left * right

            GREATER -> left > right
            GREATER_EQUAL -> left >= right
            LESS -> left < right
            LESS_EQUAL -> left <= right

            else -> throw Exception("Unreachable")
        }
    }

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? =
        if (isTruthy(evaluate(expr.condition))) {
            evaluate(expr.thenExpr)
        } else {
            evaluate(expr.elseExpr)
        }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? =
        evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): Any? =
        expr.value

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)
        return when (expr.operator.type) {
            AND -> if (isTruthy(left)) evaluate(expr.right) else left
            OR -> if (isTruthy(left)) left else evaluate(expr.right)
            else -> throw Exception("Unreachable")
        }
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)
        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields.")
        }
        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        // TODO: handle if super not present (not a subclass)
        val distance = locals[expr]!!
        val superclass = environment.getAt(distance, "super") as LoxClass
        // "super" is always 1 environment above "this"
        val instance = environment.getAt(distance - 1, "this") as LoxInstance
        val method = superclass.findMethod(expr.method.lexeme)
            ?: throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")
        return method.bind(instance)
    }

    override fun visitThisExpr(expr: Expr.This): Any? =
        lookUpVariable(expr.keyword, expr)

    override fun visitUnaryExpr(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            MINUS -> if (right is Double) {
                -right
            } else {
                throw RuntimeError(expr.operator, "Operand must be a number.")
            }

            BANG -> !isTruthy(right)
            else -> throw Exception("Unreachable")
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val function = evaluate(expr.callee)
        val arguments = expr.arguments.map(::evaluate)
        if (function !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }
        if (arguments.size != function.arity()) {
            throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
        }
        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have properties.")
        }
        return obj.get(expr.name)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? =
        lookUpVariable(expr.name, expr)

    private fun lookUpVariable(name: Token, expr: Expr): Any? =
        locals[expr].let { distance ->
            if (distance != null) {
                environment.getAt(distance, name.lexeme)
            } else {
                globals.get(name)
            }
        }

    private fun isTruthy(expr: Any?): Boolean =
        when (expr) {
            null -> false
            is Boolean -> expr
            else -> true
        }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"
        var text = value.toString()
        if (value is Double && text.endsWith(".0")) {
            text = text.substring(0, text.length - 2)
        }
        return text
    }
}
