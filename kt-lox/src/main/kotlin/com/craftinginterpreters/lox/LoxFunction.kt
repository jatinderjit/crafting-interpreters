package com.craftinginterpreters.lox

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.params.forEachIndexed { i, param ->
            environment.define(param.lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: Return) {
            return r.value
        }
        return null
    }

    override fun arity(): Int =
        declaration.params.size

    override fun toString(): String =
        "<fn ${declaration.name.lexeme}>"
}
