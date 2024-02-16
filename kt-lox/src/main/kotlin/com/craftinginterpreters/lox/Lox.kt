package com.craftinginterpreters.lox

import java.io.File
import kotlin.system.exitProcess

object Lox {
    private var hadError: Boolean = false

    fun runFile(path: String) {
        val contents = File(path).readText()
        run(contents)

        if (hadError) exitProcess(65)
    }

    fun runPrompt() {
        while (true) {
            print("> ")

            val line = readlnOrNull() ?: return
            if (line == "exit") return
            run(line)

            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expr = parser.parse()

        if (hadError) return

        println(AstPrinter.print(expr!!))
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
}
