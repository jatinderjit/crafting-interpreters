package com.craftinginterpreters.tool

import java.io.File
import kotlin.system.exitProcess

private val exprTypes = listOf(
    "Assign    : Token name, Expr value",
    "Binary    : Expr left, Token operator, Expr right",
    "Ternary   : Expr condition, Expr thenExpr, Expr elseExpr",
    "Grouping  : Expr expression",
    "Literal   : Any? value",
    "Logical   : Expr left, Token operator, Expr right",
    "Unary     : Token operator, Expr right",
    "Call      : Expr callee, Token paren, List<Expr> arguments",
    "Variable  : Token name",
)

private val stmtTypes = listOf(
    "Block       : List<Stmt> statements",
    "Expression  : Expr expression",
    "Function    : Token name, List<Token> params, List<Stmt> body",
    "If          : Expr condition, Stmt thenBranch, Stmt? elseBranch",
    "Print       : Expr expression",
    "Return      : Token keyword, Expr? value",
    "Var         : Token name, Expr? initializer",
    "While       : Expr condition, Stmt body",
)

data class TypeDefinition(val name: String, val params: List<Parameter>)

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
    GenerateAst(args[0], "Expr", exprTypes).generate()
    GenerateAst(args[0], "Stmt", stmtTypes).generate()
}

class GenerateAst(outputDir: String, private val baseName: String, private val definition: List<String>) {
    private val path = "$outputDir/$baseName.kt"
    private val w = File(path).printWriter()
    private val types = parseTypes()

    fun generate() {
        defineAst()
        w.close()
    }

    private fun parseTypes(): Array<TypeDefinition> {
        return definition.map {
            val className = it.split(":")[0].trim()
            val fields = it.split(":")[1].trim()
            val params = fields.split(",").map {
                val field = it.trim()
                val name = field.split(' ')[1]
                val type = field.split(' ')[0]
                Parameter(name, type)
            }
            TypeDefinition(className, params)
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

    private fun defineType(typeDefinition: TypeDefinition) {
        val params = typeDefinition.params.joinToString(", ") {
            "val $it"
        }
        w.println()
        w.println("    data class ${typeDefinition.name}($params): $baseName() {")
        w.println("        override fun <R> accept(visitor: Visitor<R>): R {")
        w.println("            return visitor.visit${typeDefinition.name}$baseName(this)")
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
