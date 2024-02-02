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

            hadError = true
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: Array<Token> = scanner.scanTokens()

        for (token in tokens) {
            println(token)
        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error $where: $message")
        hadError = true
    }
}
