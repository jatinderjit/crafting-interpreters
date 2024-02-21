package com.craftinginterpreters.lox

interface LoxCallable {
    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
    fun arity(): Any?
}
