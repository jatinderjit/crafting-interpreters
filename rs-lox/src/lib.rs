use std::fs::read_to_string;
use std::io::{self, BufRead, Write};

use anyhow::Result;

macro_rules! prompt {
    ($arg:expr) => {{
        print!($arg);
        std::io::stdout().flush()
    }};
}

pub fn run_file(path: &str) -> Result<()> {
    let source = read_to_string(path)?;
    run(&source)
}

pub fn run_prompt() -> Result<()> {
    let mut lines = io::stdin().lock().lines();
    loop {
        prompt!("> ")?;
        match lines.next() {
            Some(line) => run(&line?)?,
            None => return Ok(()),
        };
    }
}

fn run(source: &str) -> Result<()> {
    println!("{source}");
    Ok(())
}
