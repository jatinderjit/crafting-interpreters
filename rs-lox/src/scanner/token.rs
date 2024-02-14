#[derive(Clone, Debug)]
pub struct Token<'a> {
    pub kind: TokenKind,
    /// Start and end (exclusive) positions
    pub lexeme: &'a str,
    pub line: usize,
}

impl<'a> Token<'a> {
    pub fn new(kind: TokenKind, lexeme: &'a str, line: usize) -> Self {
        Self { kind, lexeme, line }
    }
}

#[derive(Clone, Debug, PartialEq)]
pub enum TokenKind {
    /// "("
    LeftParen,
    /// ")"
    RightParen,
    /// "{"
    LeftBrace,
    /// "}"
    RightBrace,
    /// ","
    Comma,
    /// "."
    Dot,
    /// "-"
    Minus,
    /// "+"
    Plus,
    /// ";"
    Semi,
    /// "/"
    Slash,
    /// "*"
    Star,

    // One or two character tokens.
    /// "!"
    Bang,
    /// "!="
    BangEqual,
    /// "="
    Equal,
    /// "=="
    EqualEqual,
    /// ">"
    Greater,
    /// ">="
    GreaterEqual,
    /// "<"
    Less,
    /// "<="
    LessEqual,

    // Literals.
    /// Variable function, class names.
    Identifier(String),
    /// Double quoted string: "abc"
    String(String),
    /// A number, which will always be float.
    Number(f64),

    // Keywords.
    /// Keyword "and"
    And,
    /// Keyword "class"
    Class,
    /// Keyword "else"
    Else,
    /// Keyword (boolean) "false"
    False,
    /// Keyword "fun"
    Fun,
    /// Keyword "for"
    For,
    /// Keyword "if"
    If,
    /// Keyword "nil"
    Nil,
    /// Keyword "or"
    Or,
    /// Keyword "print"
    Print,
    /// Keyword "return"
    Return,
    /// Keyword "super"
    Super,
    /// Keyword "this"
    This,
    /// Keyword "true"
    True,
    /// Keyword "var"
    Var,
    /// Keyword "while"
    While,

    /// End of File
    Eof,
}
