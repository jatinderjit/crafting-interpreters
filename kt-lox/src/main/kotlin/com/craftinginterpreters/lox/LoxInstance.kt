package com.craftinginterpreters.lox

class LoxInstance(private val klass: LoxClass) {
    private val fields = HashMap<String, Any?>()

    fun get(name: Token): Any? {
        if (name.lexeme in fields) {
            return fields[name.lexeme]
        }

        val method = klass.findMethod(name.lexeme)
        if (method != null) return method

        throw RuntimeError(name, "Undefined property '${name.lexeme}'.")
    }

    fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    override fun toString(): String =
        "<class ${klass.name} instance>"
}
