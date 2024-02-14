use super::expr::{Expr, Literal};

pub fn ast_printer(expr: &Expr) -> String {
    match expr {
        Expr::Binary { op, left, right } => parenthesize(op.lexeme, vec![left, right]),
        Expr::Grouping(expr) => parenthesize("group", vec![expr]),
        Expr::Literal(expr) => match expr {
            Literal::String(s) => s.clone(),
            Literal::Number(num) => num.to_string(),
            Literal::Bool(b) => b.to_string(),
            Literal::Nil => "nil".into(),
        },
        Expr::Unary { op, expr } => parenthesize(op.lexeme, vec![expr]),
    }
}

fn parenthesize(name: &str, exprs: Vec<&Expr>) -> String {
    let mut s = String::new();
    s.push('(');
    s.push_str(name);
    exprs.into_iter().for_each(|e| {
        s.push(' ');
        s.push_str(&ast_printer(e));
    });
    s.push(')');
    s
}

#[cfg(test)]
mod test {
    use crate::{
        ast::{
            expr::{binary, expr_number, grouping, unary},
            printer::ast_printer,
        },
        scanner::token::{Token, TokenKind},
    };

    #[test]
    fn printer() {
        let expr = binary(
            Token::new(TokenKind::Star, "*", 1),
            unary(Token::new(TokenKind::Minus, "-", 1), expr_number(123.)),
            grouping(expr_number(45.67)),
        );
        assert_eq!(ast_printer(&expr), "(* (- 123) (group 45.67))");
    }
}
