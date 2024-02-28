package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.FunctionType.*
import java.util.*

private enum class ClassType {
    NONE,
    CLASS,
}

private enum class FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD,
}

class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {
    /**
     * The boolean in the map represents if the variable has just declared, or
     * if it has been defined as well.
     *
     * It is to handle corner case `var a = a;`
     * This will be resolved in three steps:
     * - _Declare_ the variable `a`.
     * - Resolve the expression. If the value is not _defined_, raise error.
     * - _Define_ the variable `a`. Now it will be available for resolution.
     */
    private val scopes = Stack<MutableMap<String, Boolean>>()

    private var currentClass = ClassType.NONE
    private var currentFunction = NONE

    private fun resolve(expr: Expr) =
        expr.accept(this)

    private fun resolve(stmt: Stmt) =
        stmt.accept(this)

    internal fun resolve(statements: List<Stmt>) =
        statements.forEach(::resolve)

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (name.lexeme in scopes[i]) {
                // Let the interpreter know at how many steps from the top
                // environment should this variable be resolved.
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun endScope() =
        scopes.pop()

    private fun beginScope() =
        scopes.push(HashMap())

    override fun visitAssignExpr(expr: Expr.Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Expr.Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitTernaryExpr(expr: Expr.Ternary) {
        resolve(expr.condition)
        resolve(expr.thenExpr)
        resolve(expr.elseExpr)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping) =
        resolve(expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal) = Unit

    override fun visitLogicalExpr(expr: Expr.Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set) {
        resolve(expr.value)
        resolve(expr.obj)
    }

    override fun visitUnaryExpr(expr: Expr.Unary) =
        resolve(expr.right)

    override fun visitCallExpr(expr: Expr.Call) {
        resolve(expr.callee)
        expr.arguments.forEach(::resolve)
    }

    override fun visitVariableExpr(expr: Expr.Variable) {
        if (!scopes.empty() && scopes.peek()[expr.name.lexeme] == false) {
            Lox.error(expr.name, "Can't read local variable in its own initializer")
            return
        }
        resolveLocal(expr, expr.name)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS
        declare(stmt.name)
        define(stmt.name)

        // This scope is only to capture "this"
        beginScope()
        scopes.peek()["this"] = true
        stmt.methods.forEach {
            val type = if (it.name.lexeme == "init") INITIALIZER else METHOD
            resolveFunction(it, type)
        }
        endScope()

        currentClass = enclosingClass
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        // Unlike function, define the name early to allow recursion.
        define(stmt.name)
        resolveFunction(stmt, FUNCTION)
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        function.params.forEach { param ->
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()

        currentFunction = enclosingFunction
    }

    override fun visitGetExpr(expr: Expr.Get) =
        resolve(expr.obj)

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let(::resolve)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) =
        resolve(stmt.expression)

    override fun visitReturnStmt(stmt: Stmt.Return) {
        if (currentFunction == NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.")
        }
        if (stmt.value != null && currentFunction == INITIALIZER) {
            Lox.error(stmt.keyword, "Can't return a value from an initializer.")
        }
        stmt.value?.let(::resolve)
    }

    override fun visitThisExpr(expr: Expr.This) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.")
            return
        }
        resolveLocal(expr, expr.keyword)
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    private fun declare(name: Token) {
        if (scopes.empty()) return
        val scope = scopes.peek()
        if (name.lexeme in scope) {
            Lox.error(name, "Already a variable with this name in this scope.")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.empty()) return
        scopes.peek()[name.lexeme] = true
    }
}
