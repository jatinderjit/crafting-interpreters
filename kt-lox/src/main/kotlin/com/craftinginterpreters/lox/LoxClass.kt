package com.craftinginterpreters.lox

class LoxClass(internal val name: String) : LoxCallable {
    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? =
        LoxInstance(this)

    override fun arity(): Int = 0

    override fun toString(): String =
        "<class: $name>"
}
