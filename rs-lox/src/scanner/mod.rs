use crate::LoxError;

use self::{scanner::Scanner, token::Token};

mod scanner;
pub mod token;

pub fn tokenize<'a>(source: &'a str) -> (Vec<Token<'a>>, Vec<LoxError>) {
    let scanner = Scanner::new(source);
    scanner.scan_tokens()
}
