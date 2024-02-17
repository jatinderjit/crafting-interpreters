use std::{cmp::min, str::Chars};

use phf::phf_map;

use crate::LoxError;

use super::token::{
    Token,
    TokenKind::{self, *},
};

#[derive(Debug)]
pub struct Scanner<'a> {
    source: &'a str,
    chars: Chars<'a>,
    tokens: Vec<Token<'a>>,
    errors: Vec<LoxError>,
    start: usize,
    current: usize,
    line: usize,
}

static KEYWORDS: phf::Map<&'static str, TokenKind> = phf_map! {
    "and" => And,
    "class" => Class,
    "else" => Else,
    "false" => False,
    "fun" => Fun,
    "for" => For,
    "if" => If,
    "nil" => Nil,
    "or" => Or,
    "print" => Print,
    "return" => Return,
    "super" => Super,
    "this" => This,
    "true" => True,
    "var" => Var,
    "while" => While,
};

impl<'a> Scanner<'a> {
    pub fn new(source: &'a str) -> Self {
        Self {
            source,
            chars: source.chars(),
            tokens: Vec::new(),
            errors: Vec::new(),
            start: 0,
            current: 0,
            line: 1,
        }
    }

    pub fn scan_tokens(mut self) -> (Vec<Token<'a>>, Vec<LoxError>) {
        while !self.is_at_end() {
            self.start = self.current;
            self.scan_token();
        }
        self.add_token(Eof);
        (self.tokens, self.errors)
    }

    fn is_at_end(&self) -> bool {
        self.current >= self.source.len()
    }

    fn scan_token(&mut self) {
        let c = match self.advance() {
            Some(c) => c,
            None => return,
        };
        match c {
            c if is_whitespace(c) => {}
            '\n' => {
                self.line += 1;
            }
            '(' => self.add_token(LeftParen),
            ')' => self.add_token(RightParen),
            '{' => self.add_token(LeftBrace),
            '}' => self.add_token(RightBrace),
            ',' => self.add_token(Comma),
            '.' => self.add_token(Dot),
            '-' => self.add_token(Minus),
            '+' => self.add_token(Plus),
            ';' => self.add_token(Semi),
            '/' => {
                if self.match_and_advance('/') {
                    while self.peek().unwrap_or('\n') != '\n' {
                        self.advance();
                    }
                } else if self.match_and_advance('*') {
                    self.block_comment();
                } else {
                    self.add_token(Slash);
                }
            }
            '*' => self.add_token(Star),
            '!' => {
                let kind = if self.match_and_advance('=') {
                    BangEqual
                } else {
                    Bang
                };
                self.add_token(kind);
            }
            '=' => {
                let kind = if self.match_and_advance('=') {
                    EqualEqual
                } else {
                    Equal
                };
                self.add_token(kind);
            }
            '>' => {
                let kind = if self.match_and_advance('=') {
                    GreaterEqual
                } else {
                    Greater
                };
                self.add_token(kind);
            }
            '<' => {
                let kind = if self.match_and_advance('=') {
                    LessEqual
                } else {
                    Less
                };
                self.add_token(kind);
            }

            '"' => self.string(),
            c if c.is_ascii_alphabetic() => self.identifier(),
            c if c.is_ascii_digit() => self.number(),

            c => self.add_error(format!("Unexpected character: {c}")),
        };
    }

    fn add_token(&mut self, kind: TokenKind) {
        let start = min(self.start, self.source.len());
        let end = min(self.current, self.source.len());
        let lexeme = &self.source[start..end];
        let token = Token::new(kind, lexeme, self.line);
        self.tokens.push(token);
    }

    fn add_error<T: ToString>(&mut self, err: T) {
        self.errors.push((self.line, err.to_string()))
    }

    fn advance(&mut self) -> Option<char> {
        self.current += 1;
        self.chars.next()
    }

    fn match_and_advance(&mut self, c: char) -> bool {
        if self.chars.clone().next() == Some(c) {
            self.advance();
            return true;
        }
        false
    }

    fn peek(&self) -> Option<char> {
        self.chars.clone().next()
    }

    fn peek_next(&self) -> Option<char> {
        let mut chars = self.chars.clone();
        chars.next();
        chars.next()
    }

    fn string(&mut self) {
        let chars = self.chars.clone();
        while self.peek().unwrap_or('"') != '"' {
            if self.advance().unwrap() == '\n' {
                self.line += 1;
            }
        }
        if self.match_and_advance('"') {
            // We don't want the enclosing '"'
            let len = self.current - self.start - 2;
            self.add_token(TokenKind::String(chars.as_str()[..len].to_string()));
        } else {
            self.add_error("Unterminated string");
        }
    }

    fn identifier(&mut self) {
        while self
            .peek()
            .map(|c| c.is_ascii_alphanumeric())
            .unwrap_or(false)
        {
            self.advance();
        }
        let lexeme = &self.source[self.start..self.current];
        match KEYWORDS.get(lexeme) {
            Some(kind) => self.add_token(kind.clone()),
            None => self.add_token(Identifier(lexeme.to_string())),
        };
    }

    fn number(&mut self) {
        while self.peek().map(|c| c.is_ascii_digit()).unwrap_or(false) {
            self.advance();
        }
        if self.peek() == Some('.')
            && self
                .peek_next()
                .map(|c| c.is_ascii_digit())
                .unwrap_or(false)
        {
            self.advance();
            while self.peek().map(|c| c.is_ascii_digit()).unwrap_or(false) {
                self.advance();
            }
        }
        let num_str = &self.source[self.start..self.current];
        self.add_token(Number(num_str.parse().unwrap()));
    }

    fn block_comment(&mut self) {
        loop {
            match self.advance() {
                Some('*') => {
                    if self.peek() == Some('/') {
                        self.advance();
                        return;
                    } else {
                    }
                }
                Some('/') => {
                    if self.peek() == Some('*') {
                        self.advance();
                        self.block_comment();
                    }
                }
                Some(_) => {}
                None => {
                    self.add_error("Unterminated block comment");
                    return;
                }
            }
        }
    }
}

#[inline]
fn is_whitespace(c: char) -> bool {
    c == ' ' || c == '\t'
}

#[cfg(test)]
mod test {
    use std::vec;

    use crate::scanner::{
        token::TokenKind::{self, *},
        tokenize,
    };

    fn assert(src: &str, expected: Vec<TokenKind>) {
        let (tokens, _) = tokenize(src);
        let mut actual: Vec<_> = tokens.into_iter().map(|t| t.kind).collect();
        assert_eq!(actual.pop(), Some(Eof));
        assert_eq!(actual, expected);
    }

    #[test]
    fn single_char_tokens() {
        let src = "(){},.+-;/ *// *******";
        let expected = vec![
            LeftParen, RightParen, LeftBrace, RightBrace, Comma, Dot, Plus, Minus, Semi, Slash,
            Star,
        ];
        assert(src, expected);
    }

    #[test]
    fn one_two_char_tokens() {
        let src = "=! != == <<=>>=";
        let expected = vec![
            Equal,
            Bang,
            BangEqual,
            EqualEqual,
            Less,
            LessEqual,
            Greater,
            GreaterEqual,
        ];
        assert(src, expected);
    }

    #[test]
    fn string() {
        let src = "(\"abc\", \"\")";
        let expected = vec![
            LeftParen,
            String("abc".to_string()),
            Comma,
            String("".to_string()),
            RightParen,
        ];
        assert(src, expected);
    }

    #[test]
    fn string_errors() {
        let src = "print false;
            var x = \"abc";
        let (tokens, errors) = tokenize(src);

        let kinds: Vec<_> = tokens.into_iter().map(|t| t.kind).collect();
        let expected = vec![Print, False, Semi, Var, Identifier("x".into()), Equal, Eof];
        assert_eq!(kinds, expected);

        assert_eq!(errors.len(), 1);
        let (line, msg) = errors[0];
        assert_eq!(line, 2);
        assert_eq!(msg, "Unterminated string");
    }

    #[test]
    fn identifier() {
        let src = "name
age";
        let expected = vec![
            Identifier("name".to_string()),
            Identifier("age".to_string()),
        ];
        assert(src, expected);
    }

    #[test]
    fn keywords() {
        let src = "and class else false fun for if nil or print return super this true var while";
        let expected = vec![
            And, Class, Else, False, Fun, For, If, Nil, Or, Print, Return, Super, This, True, Var,
            While,
        ];
        assert(src, expected);
    }

    #[test]
    fn number() {
        let src = "0123, 0.45, 5.";
        let expected = vec![Number(123.), Comma, Number(0.45), Comma, Number(5.), Dot];
        assert(src, expected);
    }

    #[test]
    fn block_comment() {
        let src = "var x = /*** mutline
            block comment ** / */ nil";
        let expected = vec![Var, Identifier("x".into()), Equal, Nil];
        assert(src, expected);

        // nested block comment
        let src = "print /*** /**/ */;";
        assert(src, vec![Print, Semi]);

        let src = "print /*** /* */";
        let (tokens, errors) = tokenize(src);
        let token_kinds: Vec<_> = tokens.into_iter().map(|t| t.kind).collect();
        assert_eq!(token_kinds, vec![Print, Eof]);
        assert_eq!(errors.len(), 1);
        assert_eq!(errors[0].1, "Unterminated block comment");
    }

    #[test]
    #[rustfmt::skip::macros(vec)]
    fn programs() {
        let src = "var x = \"abc\";
            print x;
            var y = 5.1 * 4;
            var z = -1 / 4;
            while(true) {
              // Some comment
            }
            fun add(a, b) { return a + b; }";
        let expected = vec![
            Var, Identifier("x".into()), Equal, String("abc".into()), Semi,
            Print, Identifier("x".into()), Semi,
            Var, Identifier("y".into()), Equal, Number(5.1), Star, Number(4.), Semi,
            Var, Identifier("z".into()), Equal, Minus, Number(1.), Slash, Number(4.), Semi,
            While, LeftParen, True, RightParen, LeftBrace, RightBrace,

            Fun, Identifier("add".into()), LeftParen, Identifier("a".into()), Comma,
            Identifier("b".into()), RightParen, LeftBrace, Return,
            Identifier("a".into()), Plus, Identifier("b".into()), Semi, RightBrace,
        ];
        assert(src, expected);
    }
}
