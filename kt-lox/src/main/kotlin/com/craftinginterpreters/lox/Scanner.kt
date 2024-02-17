package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

private val keywords = hashMapOf(
    "and" to AND,
    "class" to CLASS,
    "else" to ELSE,
    "false" to FALSE,
    "fun" to FUN,
    "for" to FOR,
    "if" to IF,
    "nil" to NIL,
    "or" to OR,
    "print" to PRINT,
    "return" to RETURN,
    "super" to SUPER,
    "this" to THIS,
    "true" to TRUE,
    "var" to VAR,
    "while" to WHILE,
)

class Scanner(private val source: String) {
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1
    private val tokens = mutableListOf<Token>()

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '/' -> {
                if (match('/')) {
                    while (peek() != null && peek() != '\n') advance()
                } else addToken(SLASH)
            }

            '*' -> addToken(STAR)
            '?' -> addToken(QUESTION_MARK)
            ':' -> addToken(COLON)

            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '"' -> string()
            else -> when {
                isAlpha(c) -> identifier()
                isDigit(c) -> number()
                else -> Lox.error(line, "Unexpected characters")
            }

        }
    }

    /**
     * Parses the double-quoted strings and adds the token along with the literal.
     *
     * Escape sequences (`\n`, `\t`, etc.) are not supported. Currently, there is no way
     * to add the double quote (`"`) character in the string.
     *
     * Multi-line strings are supported.
     */
    private fun string() {
        while (!isAtEnd() && peek() != '"') {
            if (advance() == '\n') line++
        }
        if (!match('"')) {
            Lox.error(line, "Unterminated string.")
            return
        }
        addToken(STRING, source.substring(start + 1, current - 1))
    }

    private fun number() {
        while (isDigit(peek())) advance()
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val lexeme = source.substring(start, current)
        addToken(keywords[lexeme] ?: IDENTIFIER)
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(): Char? {
        if (isAtEnd()) return null
        return source[current]
    }

    private fun peekNext(): Char? {
        if (current + 1 >= source.length) return null
        return source[current + 1]
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun match(c: Char): Boolean {
        if (peek() != c) return false
        advance()
        return true
    }

    private fun isAlpha(c: Char?): Boolean {
        return (c in 'A'..'Z')
                || (c in 'a'..'z')
                || c == '_'
    }

    private fun isDigit(c: Char?): Boolean {
        return c in '0'..'9'
    }

    private fun isAlphaNumeric(c: Char?): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val lexeme = source.substring(start, current)
        tokens.add(Token(type, lexeme, literal, line))
    }
}
