package com.craftinginterpreters.lox

class LoxInstance(private val klass: LoxClass) {
    override fun toString(): String =
        "<class ${klass.name} instance>"
}
