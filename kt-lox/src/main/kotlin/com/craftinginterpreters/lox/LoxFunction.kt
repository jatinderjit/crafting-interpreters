package com.craftinginterpreters.lox

class LoxFunction(
    private val declaration: Stmt.Function,
    private val closure: Environment,
    private val isInitializer: Boolean,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.params.forEachIndexed { i, param ->
            environment.define(param.lexeme, arguments[i])
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (r: Return) {
            if (isInitializer) return closure.getAt(0, "this")
            return r.value
        }
        if (isInitializer) return closure.getAt(0, "this")
        return null
    }

    override fun arity(): Int =
        declaration.params.size

    override fun toString(): String =
        "<fn ${declaration.name.lexeme}>"

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }
}
