package com.craftinginterpreters.lox

class Environment(private val enclosing: Environment? = null) {
    private val values = HashMap<String, Any?>()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun get(name: Token): Any? {
        if (name.lexeme in values) {
            return values[name.lexeme]
        }
        if (enclosing != null) {
            return enclosing.get(name)
        }
        throw RuntimeError(name, "Undefined variable ${name.lexeme}")
    }

    fun getAt(distance: Int, name: Token): Any? =
        ancestor(distance).values[name.lexeme]

    fun assign(name: Token, value: Any?) {
        if (name.lexeme in values) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) {
            return enclosing.assign(name, value)
        }
        throw RuntimeError(name, "Undefined variable ${name.lexeme}")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment = this
        for (i in 0..<distance) {
            environment = environment.enclosing!!
        }
        return environment
    }
}
