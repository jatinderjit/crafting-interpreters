package com.craftinginterpreters.lox

import java.util.*

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

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        resolve(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        declare(stmt.name)
        // Unlike function, define the name early to allow recursion.
        define(stmt.name)
        resolveFunction(stmt)
    }

    private fun resolveFunction(function: Stmt.Function) {
        beginScope()
        function.params.forEach { param ->
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        stmt.elseBranch?.let(::resolve)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) =
        resolve(stmt.expression)

    override fun visitReturnStmt(stmt: Stmt.Return) {
        stmt.value?.let(::resolve)
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
        scopes.peek()[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.empty()) return
        scopes.peek()[name.lexeme] = true
    }
}
