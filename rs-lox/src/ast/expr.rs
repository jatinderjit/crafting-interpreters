use crate::scanner::token::Token;

pub enum Expr<'a> {
    Binary {
        op: Token<'a>,
        left: Box<Expr<'a>>,
        right: Box<Expr<'a>>,
    },
    Grouping(Box<Expr<'a>>),
    Literal(Literal),
    Unary {
        op: Token<'a>,
        expr: Box<Expr<'a>>,
    },
}

pub enum Literal {
    String(String),
    Number(f64),
    Bool(bool),
    Nil,
}

pub fn binary<'a>(op: Token<'a>, left: Expr<'a>, right: Expr<'a>) -> Expr<'a> {
    Expr::Binary {
        op,
        left: Box::new(left),
        right: Box::new(right),
    }
}

pub fn grouping<'a>(expr: Expr<'a>) -> Expr<'a> {
    Expr::Grouping(Box::new(expr))
}

pub fn unary<'a>(op: Token<'a>, expr: Expr<'a>) -> Expr<'a> {
    Expr::Unary {
        op,
        expr: Box::new(expr),
    }
}

pub fn expr_string(val: String) -> Expr<'static> {
    Expr::Literal(Literal::String(val))
}

pub fn expr_number(val: f64) -> Expr<'static> {
    Expr::Literal(Literal::Number(val))
}

pub fn expr_bool(val: bool) -> Expr<'static> {
    Expr::Literal(Literal::Bool(val))
}

pub const EXPR_NIL: Expr<'static> = Expr::Literal(Literal::Nil);
