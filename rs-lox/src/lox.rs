use anyhow::Result;

use crate::scanner::tokenize;

#[derive(Debug)]
pub(crate) struct Lox {
    had_error: bool,
}

impl Lox {
    pub(crate) fn new() -> Self {
        Self { had_error: false }
    }
    pub(crate) fn run(&mut self, source: &str) -> Result<()> {
        let (tokens, errors) = tokenize(source);
        dbg!(tokens);
        errors
            .into_iter()
            .for_each(|(line, message)| self.error(line, message));
        Ok(())
    }

    pub(crate) fn error(&self, line: usize, message: &str) {
        self.report(line, "", message);
    }

    pub(crate) fn report(&self, line: usize, loc: &str, message: &str) {
        println!("[line {line}] Error {loc}: {message}")
    }
}
