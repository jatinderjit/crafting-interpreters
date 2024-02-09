package com.craftinginterpreters.lox

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: lox [script]")
        // Ref for exit codes:
        // https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
        exitProcess(64)
    }
    if (args.size == 1) Lox.runFile(args[0])
    else Lox.runPrompt()
}
