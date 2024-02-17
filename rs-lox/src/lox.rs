use anyhow::{bail, Result};

use crate::scanner::tokenize;

#[derive(Debug)]
pub struct Lox {
    pub had_error: bool,
}

impl Lox {
    pub fn new() -> Self {
        Self { had_error: false }
    }
    pub fn run(&mut self, source: &str) -> Result<()> {
        let (tokens, errors) = tokenize(source);
        dbg!(tokens);
        self.had_error = !errors.is_empty();
        errors
            .into_iter()
            .for_each(|(line, message)| self.error(line, &message));
        if self.had_error {
            bail!("Lox encountered errors");
        }
        Ok(())
    }

    pub(crate) fn error(&self, line: usize, message: &str) {
        self.report(line, "", message);
    }

    pub(crate) fn report(&self, line: usize, loc: &str, message: &str) {
        println!("[line {line}] Error {loc}: {message}")
    }
}
