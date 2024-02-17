package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.TokenType.*
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ScannerTest {
    @Test
    fun testSingleCharacterTokens() {
        val source = "(  )\n{},.-+;*/?:"
        val expected = listOf(
            LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS,
            SEMICOLON, STAR, SLASH, QUESTION_MARK, COLON, EOF
        )
        val tokenTypes = Scanner(source).scanTokens().map(Token::type)
        assertContentEquals(expected, tokenTypes)
    }

    @Test
    fun testComment() {
        val source = "(/+//+)"
        val expected = listOf(LEFT_PAREN, SLASH, PLUS, EOF)
        val tokenTypes = Scanner(source).scanTokens().map(Token::type)
        assertContentEquals(expected, tokenTypes)
    }

    @Test
    fun testSamePrefixTokens() {
        val source = "! != < > <= >= = =="
        val expected = listOf(
            BANG, BANG_EQUAL, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EQUAL,
            EQUAL_EQUAL, EOF
        )
        val tokenTypes = Scanner(source).scanTokens().map(Token::type)
        assertContentEquals(expected, tokenTypes)
    }

    @Test
    fun testIdentifiers() {
        val source = "(function classy VAR)"
        val expected = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(IDENTIFIER, "function", null, 1),
            Token(IDENTIFIER, "classy", null, 1),
            Token(IDENTIFIER, "VAR", null, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(EOF, "", null, 1),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)
    }

    @Test
    fun testString() {
        val source = "/\"abc\""
        val expected = listOf(
            Token(SLASH, "/", null, 1),
            Token(STRING, "\"abc\"", "abc", 1),
            Token(EOF, "", null, 1),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)
    }

    @Test
    fun testUnterminatedString() {
        val source = "(\"abc)"
        val expected = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(EOF, "", null, 1),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)

        // TODO test stderr
    }

    @Test
    fun testNumber() {
        val source = "\"a0\"a1 23 45.67"
        val expected = listOf(
            Token(STRING, "\"a0\"", "a0", 1),
            Token(IDENTIFIER, "a1", null, 1),
            Token(NUMBER, "23", 23.0, 1),
            Token(NUMBER, "45.67", 45.67, 1),
            Token(EOF, "", null, 1),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)
    }

    @Test
    fun testKeywords() {
        val source = "and class classes else false fun for if nil or print return super this true var while"
        val expected = listOf(
            Token(AND, "and", null, 1),
            Token(CLASS, "class", null, 1),
            Token(IDENTIFIER, "classes", null, 1),
            Token(ELSE, "else", null, 1),
            Token(FALSE, "false", null, 1),
            Token(FUN, "fun", null, 1),
            Token(FOR, "for", null, 1),
            Token(IF, "if", null, 1),
            Token(NIL, "nil", null, 1),
            Token(OR, "or", null, 1),
            Token(PRINT, "print", null, 1),
            Token(RETURN, "return", null, 1),
            Token(SUPER, "super", null, 1),
            Token(THIS, "this", null, 1),
            Token(TRUE, "true", null, 1),
            Token(VAR, "var", null, 1),
            Token(WHILE, "while", null, 1),
            Token(EOF, "", null, 1),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)
    }

    @Test
    fun testLineNumbers() {
        val source = """
            a 1.2
            false
        """.trimIndent()
        val expected = listOf(
            Token(IDENTIFIER, "a", null, 1),
            Token(NUMBER, "1.2", 1.2, 1),
            Token(FALSE, "false", null, 2),
            Token(EOF, "", null, 2),
        )
        val tokens = Scanner(source).scanTokens()
        assertContentEquals(expected, tokens)
    }

    @Test
    fun testInvalidChars() {
        val source = "(a 1 ^,% )"
        val tokens = Scanner(source).scanTokens()
        val expected = listOf(
            Token(LEFT_PAREN, "(", null, 1),
            Token(IDENTIFIER, "a", null, 1),
            Token(NUMBER, "1", 1.0, 1),
            Token(COMMA, ",", null, 1),
            Token(RIGHT_PAREN, ")", null, 1),
            Token(EOF, "", null, 1),
        )
        assertContentEquals(expected, tokens)

        // TODO test stderr
    }
}
