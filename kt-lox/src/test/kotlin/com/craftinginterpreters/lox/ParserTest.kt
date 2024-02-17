package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Expr.*
import com.craftinginterpreters.lox.TokenType.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    private val plus = Token(PLUS, "+", null, 1)
    private val minus = Token(MINUS, "-", null, 1)
    private val bang = Token(BANG, "!", null, 1)
    private val star = Token(STAR, "*", null, 1)
    private val slash = Token(SLASH, "/", null, 1)
    private val less = Token(LESS, "<", null, 1)
    private val lessEqual = Token(LESS_EQUAL, "<=", null, 1)
    private val greater = Token(GREATER, ">", null, 1)
    private val greaterEqual = Token(GREATER_EQUAL, ">=", null, 1)
    private val equalEqual = Token(EQUAL_EQUAL, "==", null, 1)
    private val bangEqual = Token(BANG_EQUAL, "!=", null, 1)
    private val comma = Token(COMMA, ",", null, 1)

    private fun parse(source: String): Expr? {
        val tokens = Scanner(source).scanTokens()
        return Parser(tokens).parse()
    }

    private fun assertExpr(source: String, expected: Expr?) {
        val actual = parse(source)
        assertEquals(actual, expected)
    }

    @Test
    fun primary() {
        assertExpr("123", Literal(123.0))
        assertExpr("\"123\"", Literal("123"))
        assertExpr("true", Literal(true))
        assertExpr("false", Literal(false))
        assertExpr("nil", Literal(null))
    }

    @Test
    fun unary() {
        assertExpr("-123", Unary(minus, Literal(123.0)))
        assertExpr("!!false", Unary(bang, Unary(bang, Literal(false))))
    }

    @Test
    fun factor() {
        val source = "-4 * 5 / 10"
        val expected = Binary(
            Binary(Unary(minus, Literal(4.0)), star, Literal(5.0)),
            slash,
            Literal(10.0),
        )
        assertExpr(source, expected)
    }

    @Test
    fun term() {
        /*
                    -
                   / \
                  -   1
                 / \
                +   2
               / \
              5   *
                 / \
                4   3
         */
        val source = "5 + 4 * 3 - 2 - 1"
        val expected = Binary(
            Binary(
                Binary(
                    Literal(5.0),
                    plus,
                    Binary(Literal(4.0), star, Literal(3.0)),
                ),
                minus,
                Literal(2.0),
            ),
            minus,
            Literal(1.0),
        )
        assertExpr(source, expected)
    }

    @Test
    fun comparison() {
        assertExpr("4 < 5", Binary(Literal(4.0), less, Literal(5.0)))
        assertExpr("4 <= 5", Binary(Literal(4.0), lessEqual, Literal(5.0)))
        assertExpr("5 > 4", Binary(Literal(5.0), greater, Literal(4.0)))
        assertExpr("5 >= 4", Binary(Literal(5.0), greaterEqual, Literal(4.0)))
    }

    @Test
    fun equality() {
        val source = "4 >= 2.1 == true != false"
        val expected = Binary(
            Binary(
                Binary(Literal(4.0), greaterEqual, Literal(2.1)),
                equalEqual,
                Literal(true),
            ),
            bangEqual,
            Literal(false)
        )
        assertExpr(source, expected)
    }

    @Test
    fun comma() {
        val source = "4+2,1<5"
        val expected = Binary(
            Binary(Literal(4.0), plus, Literal(2.0)),
            comma,
            Binary(Literal(1.0), less, Literal(5.0))
        )
        assertExpr(source, expected)
    }

    @Test
    fun ternary() {
        // ((1 + 2) ? ((3 - 2) ? (4 * 5) : nil) : (6 ? 7 : 8)), 9
        val source = "1 + 2 ? 3 - 2 ? 4 * 5 : nil : 6 ? 7 : 8, 9 ? 10 : 11"
        val expected = Binary(
            Ternary(
                Binary(Literal(1.0), plus, Literal(2.0)),
                Ternary(
                    Binary(Literal(3.0), minus, Literal(2.0)),
                    Binary(Literal(4.0), star, Literal(5.0)),
                    Literal(null),
                ),
                Ternary(Literal(6.0), Literal(7.0), Literal(8.0))
            ),
            comma,
            Ternary(Literal(9.0), Literal(10.0), Literal(11.0))
        )
        assertExpr(source, expected)
    }

    @Test
    fun grouping() {
        val source = "4 * (2 + 3) / (2 - 1)"
        val expected = Binary(
            Binary(
                Literal(4.0),
                star,
                Grouping(Binary(Literal(2.0), plus, Literal(3.0))),
            ),
            slash,
            Grouping(Binary(Literal(2.0), minus, Literal(1.0)))
        )
        assertExpr(source, expected)
    }
}
