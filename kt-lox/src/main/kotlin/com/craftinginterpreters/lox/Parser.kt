package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*

class ParseError : RuntimeException()

/**
 * Grammar:
 *
 * ```
 * program        → statement* EOF ;
 *
 * declaration    → varDecl
 *                | statement ;
 *
 * statement      → exprStmt
 *                | ifStmt
 *                | printStmt
 *                | whileStmt
 *                | block ;
 *
 * ifStmt         → "if" "(" expression ")" statement
 *                  ( "else" statement )? ;
 *
 * whileStmt      → "while" "(" expression ")" statement ;
 *
 * block          → "{" declaration* "}"
 *
 * varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
 *
 * exprStmt       → expression ";" ;
 *
 * printStmt      → "print" expression ";" ;
 *
 * expression     → comma ;
 *
 * comma          → assignment ( "," assignment)* ;
 *
 * assignment     → IDENTIFIER "=" assignment
 *                | ternary ;
 *
 * ternary        → equality "?" ternary ":" ternary
 *                | equality ;
 *
 * logical_or     → logic_and ( "or" logic_and )* ;
 *
 * logical_and    → equality ( "and" equality )* ;
 *
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 *
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 *
 * term           → factor ( ( "-" | "+" ) factor )* ;
 *
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 *
 * unary          → ( "!" | "-" ) unary | call ;
 *
 * call           → primary ( "(" arguments? ")" ) ;
 * arguments      → expression ( "," expression? )* ;
 *
 * primary        → "true" | "false" | "nil"
 *                | NUMBER | STRING
 *                | "(" expression ")"
 *                | IDENTIFIER ;
 *  ```
 */
class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            try {
                statements.add(declaration())
            } catch (error: ParseError) {
                synchronize()
            }
        }
        return statements
    }

    private fun declaration(): Stmt {
        if (match(VAR)) return varDeclaration()
        return statement()
    }

    private fun varDeclaration(): Stmt {
        val name = consume(IDENTIFIER, "Expect variable name.")

        var initializer: Expr? = null
        if (match(EQUAL)) {
            initializer = expression()
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(): Stmt {
        if (match(FOR)) return forStatement()
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(WHILE)) return whileStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun block(): List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun expressionStatement(): Stmt {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    /**
     * Syntactic sugar. For loop is converted to a while loop.
     *
     * `for (var i = 0; i < 10; i = i + 10) print i;`
     *
     * boils down to:
     *
     * ```js
     * {
     *   var i = 0;
     *   while (i < 10) {
     *     print i;
     *     i = i + 1;
     *   }
     * }
     * ```
     */
    private fun forStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after for.")
        val initializer = when {
            match(SEMICOLON) -> null
            match(VAR) -> varDeclaration()
            else -> expressionStatement()
        }

        val condition = if (check(SEMICOLON)) null else expression()
        consume(SEMICOLON, "Expect ';' after loop condition.")

        val increment = if (check(RIGHT_PAREN)) null else expression()
        consume(RIGHT_PAREN, "Expect ')' after for clauses.")

        var body = statement()
        if (increment != null) {
            body = Stmt.Block(listOf(body, Stmt.Expression(increment)))
        }
        body = Stmt.While(condition ?: Expr.Literal(true), body)
        if (initializer != null) {
            body = Stmt.Block(listOf(initializer, body))
        }
        return body
    }

    private fun ifStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after if.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        val elseBranch = if (match(ELSE)) statement() else null
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement(): Stmt {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun whileStatement(): Stmt {
        consume(LEFT_PAREN, "Expect '(' after while.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()
        return Stmt.While(condition, body)
    }

    private fun expression(): Expr {
        return comma()
    }

    private fun comma(): Expr {
        var expr = assignment()
        while (match(COMMA)) {
            val operator = previous()
            val right = assignment()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun assignment(): Expr {
        val expr = ternary()
        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()
            if (expr is Expr.Variable) {
                return Expr.Assign(expr.name, value)
            }
            // Report an error, without throwing it.
            // Parser isn't in a confused state. We don't need to go into panic
            // mode and synchronize.
            error(equals, "Invalid assignment target.")
        }
        return expr
    }

    private fun ternary(): Expr {
        var expr = or()
        if (match(QUESTION_MARK)) {
            val ifExpr = ternary()
            consume(COLON, "Expect else (\":\") condition.")
            val elseExpr = ternary()
            expr = Expr.Ternary(expr, ifExpr, elseExpr)
        }
        return expr
    }

    private fun or(): Expr {
        var expr = and()
        while (match(OR)) {
            val token = previous()
            val right = and()
            expr = Expr.Logical(expr, token, right)
        }
        return expr
    }

    private fun and(): Expr {
        var expr = equality()
        while (match(AND)) {
            val token = previous()
            val right = equality()
            expr = Expr.Logical(expr, token, right)
        }
        return expr
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
        return call()
    }

    private fun call(): Expr {
        var expr = primary()
        // Handle chain of function calls. Example: `f(1)(2)`
        while (match(LEFT_PAREN)) {
            expr = finishCall(expr)
        }
        return expr
    }

    private fun finishCall(callee: Expr): Expr {
        val arguments = mutableListOf<Expr>()
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments.")
                }
                arguments.add(expression())
            } while (match(COMMA))
        }
        val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
        return Expr.Call(callee, paren, arguments)
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)
        if (match(IDENTIFIER)) return Expr.Variable(previous())

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression.")
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
            if (previous().type == SEMICOLON) return
            when (peek().type) {
                CLASS -> return
                FUN -> return
                FOR -> return
                IF -> return
                PRINT -> return
                RETURN -> return
                VAR -> return
                WHILE -> return
                else -> advance()
            }
        }
    }
}
