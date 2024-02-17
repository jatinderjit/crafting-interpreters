package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

object Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
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

    private fun evaluate(expr: Expr): Any? = expr.accept(this)

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        TODO("Not yet implemented")
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
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
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
            }
        }

        if (left !is Double || right !is Double) {
            throw RuntimeError(expr.operator, "Operands must be numbers")
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

    override fun visitTernaryExpr(expr: Expr.Ternary): Any? {
        return if (isTruthy(evaluate(expr.condition))) {
            evaluate(expr.if_expr)
        } else {
            evaluate(expr.else_expr)
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
