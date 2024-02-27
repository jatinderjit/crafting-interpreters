package com.craftinginterpreters.lox

class LoxClass(
    internal val name: String,
    private val methods: MutableMap<String, LoxFunction>,
) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): LoxInstance =
        LoxInstance(this)

    override fun arity(): Int = 0

    fun findMethod(name: String): LoxFunction? =
        methods[name]

    override fun toString(): String =
        "<class: $name>"
}
