package com.craftinginterpreters.lox

object AstPrinter : Expr.Visitor<String> {
    fun print(expr: Expr): String =
        expr.accept(this)

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder()
        builder.append('(').append(name)
        exprs.forEach {
            builder.append(' ')
            builder.append(it.accept(this))
        }
        builder.append(')')
        return builder.toString()
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String =
        parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitTernaryExpr(expr: Expr.Ternary): String =
        parenthesize("?:", expr.condition, expr.if_expr, expr.else_expr)

    override fun visitGroupingExpr(expr: Expr.Grouping): String =
        parenthesize("group", expr.expression)


    override fun visitLiteralExpr(expr: Expr.Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String =
        parenthesize(expr.operator.lexeme, expr.right)

    override fun visitVariableExpr(expr: Expr.Variable): String =
        "(= ${expr.name})"
}
