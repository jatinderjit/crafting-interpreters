package com.craftinginterpreters.lox

abstract class Expr {
    interface Visitor<R> {
        fun visitBinaryExpr(expr: Binary): R
        fun visitTernaryExpr(expr: Ternary): R
        fun visitGroupingExpr(expr: Grouping): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitVariableExpr(expr: Variable): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    data class Binary(val left: Expr, val operator: Token, val right: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    data class Ternary(val condition: Expr, val if_expr: Expr, val else_expr: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitTernaryExpr(this)
        }
    }

    data class Grouping(val expression: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    data class Literal(val value: Any?): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    data class Unary(val operator: Token, val right: Expr): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }

    data class Variable(val name: Token): Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }
}
