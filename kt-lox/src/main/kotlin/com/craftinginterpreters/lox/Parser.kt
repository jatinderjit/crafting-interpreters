package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

class ParseError : RuntimeException()

/**
 * Grammar:
 *
 * xpression     → equality ;
 *
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 *
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 *
 * term           → factor ( ( "-" | "+" ) factor )* ;
 *
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 *
 * unary          → ( "!" | "-" ) unary
 *                | primary ;
 *
 * primary        → NUMBER | STRING | "true" | "false" | "nil"
 *                | "(" expression ")" ;
 */
class Parser(val tokens: List<Token>) {


    private var current = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (_: ParseError) {
            null
        }
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(): Expr {
        var expr = term()
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = unary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val expr = unary()
            return Expr.Unary(operator, expr)
        }
        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun match(vararg types: TokenType): Boolean {
        types.forEach {
            if (check(it)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            when (peek().type) {
                CLASS -> return
                FUN -> return
                FOR -> return
                IF -> return
                PRINT -> return
                RETURN -> return
                VAR -> return
                WHILE -> return
                else -> {}
            }
        }

        advance()
    }
}
