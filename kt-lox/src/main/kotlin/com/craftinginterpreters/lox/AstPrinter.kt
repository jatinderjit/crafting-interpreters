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

    override fun visitAssignExpr(expr: Expr.Assign): String =
        "(= ${expr.name} ${expr.value.accept(this)})"

    override fun visitBinaryExpr(expr: Expr.Binary): String =
        parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitTernaryExpr(expr: Expr.Ternary): String =
        parenthesize("?:", expr.condition, expr.thenExpr, expr.elseExpr)

    override fun visitGroupingExpr(expr: Expr.Grouping): String =
        parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String =
        parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitSetExpr(expr: Expr.Set): String =
        "(set ${expr.obj.accept(this)}.${expr.name} ${expr.value})"

    override fun visitThisExpr(expr: Expr.This): String =
        "this"

    override fun visitUnaryExpr(expr: Expr.Unary): String =
        parenthesize(expr.operator.lexeme, expr.right)

    override fun visitCallExpr(expr: Expr.Call): String =
        parenthesize("call", expr.callee, *expr.arguments.toTypedArray())

    override fun visitGetExpr(expr: Expr.Get): String =
        "(get ${expr.obj}.${expr.name})"

    override fun visitVariableExpr(expr: Expr.Variable): String =
        "(var ${expr.name})"
}
