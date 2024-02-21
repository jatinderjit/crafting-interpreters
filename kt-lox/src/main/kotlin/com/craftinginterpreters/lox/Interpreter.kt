package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

object Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private var environment = globals

    init {
        builtins()
    }

    private fun builtins() {
        globals.define("clock", object : LoxCallable {
            /**
             * Time in seconds
             */
            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? =
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

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val prevEnvironment = this.environment
        this.environment = environment
        try {
            statements.forEach(::execute)
        } finally {
            this.environment = prevEnvironment
        }
    }

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
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
            return if (left is String && right is String) {
                left + right
            } else if (left is Double && right is Double) {
                left + right
            } else {
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
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

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        return if (isTruthy(evaluate(expr.condition))) {
            evaluate(expr.if_expr)
        } else {
            evaluate(expr.else_expr)
        }
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)
        return when (expr.operator.type) {
            AND -> if (isTruthy(left)) evaluate(expr.right) else left
            OR -> if (isTruthy(left)) left else evaluate(expr.right)
            else -> throw Exception("Unreachable")
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
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

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    private fun isTruthy(expr: Any?): Boolean {
        if (expr == null) return false
        if (expr is Boolean) return expr
        return true
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
