pub mod ast;
pub mod lox;
pub mod scanner;

use std::fs::read_to_string;
use std::io::{self, BufRead, Write};

use anyhow::Result;

use lox::Lox;

type LoxError = (usize, &'static str);

macro_rules! prompt {
    ($arg:expr) => {{
        print!($arg);
        std::io::stdout().flush()
    }};
}

pub fn run_file(path: &str) -> Result<()> {
    let mut lox = Lox::new();
    let source = read_to_string(path)?;
    lox.run(&source)
}

pub fn run_prompt() -> Result<()> {
    let mut lox = Lox::new();
    let mut lines = io::stdin().lock().lines();
    loop {
        prompt!("> ")?;
        let line = match lines.next() {
            Some(line) => line,
            None => return Ok(()),
        };
        let _ = lox.run(&line?);
        lox.had_error = false;
    }
}
