package com.craftinginterpreters.lox

class LoxClass(
    internal val name: String,
    private val superclass: LoxClass?,
    private val methods: MutableMap<String, LoxFunction>,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun arity(): Int {
        val initializer = findMethod("init")
        return initializer?.arity() ?: 0
    }

    fun findMethod(name: String): LoxFunction? =
        methods[name] ?: superclass?.findMethod(name)

    override fun toString(): String =
        "<class: $name>"
}
