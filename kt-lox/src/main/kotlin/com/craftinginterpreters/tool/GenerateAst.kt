package com.craftinginterpreters.lox.tool

import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess

private const val baseName = "Expr"

private val exprTypes = arrayOf(
    "Binary    : Expr left, Token operator, Expr right",
    "Grouping  : Expr expression",
    "Literal   : Any? value",
    "Unary     : Token operator, Expr right",
)

data class ExprType(val name: String, val params: Array<Parameter>)

data class Parameter(val name: String, val type: String) {
    override fun toString(): String {
        return "$name: $type"
    }
}

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: generate_ast <output directory>")
        exitProcess(64)
    }
    GenerateAst(args[0]).generate()
}

class GenerateAst(outputDir: String) {
    private val path = "$outputDir/$baseName.kt"
    private var w: PrintWriter = File(path).printWriter()
    private lateinit var types: Array<ExprType>

    fun generate() {
        types = parseTypes()
        defineAst()
        w.close()
    }

    private fun parseTypes(): Array<ExprType> {
        return exprTypes.map<String, ExprType> {
            val className = it.split(":")[0].trim()
            val fields = it.split(":")[1].trim()
            val params = fields.split(",").map {
                val field = it.trim()
                val name = field.split(' ')[1]
                val type = field.split(' ')[0]
                Parameter(name, type)
            }.toTypedArray()
            ExprType(className, params)
        }.toTypedArray()
    }

    private fun defineAst() {
        w.println("package com.craftinginterpreters.lox")
        w.println()
        w.println("abstract class $baseName {")
        defineVisitor()
        w.println()
        w.println("    abstract fun <R> accept(visitor: Visitor<R>): R")
        println()
        types.forEach(::defineType)
        w.println("}")
    }

    private fun defineType(exprType: ExprType) {
        val params = exprType.params.joinToString(", ") {
            "val $it"
        }
        w.println()
        w.println("    data class ${exprType.name}($params): $baseName() {")
        w.println("        override fun <R> accept(visitor: Visitor<R>): R {")
        w.println("            return visitor.visit${exprType.name}$baseName(this)")
        w.println("        }")
        w.println("    }")
    }

    private fun defineVisitor() {
        w.println("    interface Visitor<R> {")
        types.forEach {
            w.println("        fun visit${it.name}$baseName(${baseName.lowercase()}: ${it.name}): R")
        }
        w.println("    }")
    }
}
