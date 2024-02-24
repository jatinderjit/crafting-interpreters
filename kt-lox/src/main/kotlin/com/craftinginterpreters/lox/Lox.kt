package com.craftinginterpreters.lox

import java.io.File
import kotlin.system.exitProcess

object Lox {
    private var hadError: Boolean = false
    private var hadRuntimeError: Boolean = false
    private val interpreter = Interpreter()

    fun runFile(path: String) {
        val contents = File(path).readText()
        run(contents)

        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        while (true) {
            print("> ")

            val line = readlnOrNull() ?: return
            if (line == "exit") return
            run(line)

            hadError = false
            hadRuntimeError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        val parser = Parser(tokens)
        val statements = parser.parse()
        if (hadError) return

        val resolver = Resolver(interpreter)
        resolver.resolve(statements)
        if (hadError) return

        interpreter.interpret(statements)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        System.err.println("[line ${error.token.line}] Error: ${error.message}")
        hadRuntimeError = true
    }
}
