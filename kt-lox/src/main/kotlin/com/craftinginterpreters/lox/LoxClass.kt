package com.craftinginterpreters.lox

class LoxClass(private val name: String) {
    override fun toString(): String =
        "<class: $name>"
}
