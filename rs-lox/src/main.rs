use anyhow::Result;
use lox::{run_file, run_prompt};
use std::{env, process::exit};

fn main() -> Result<()> {
    let args = env::args().collect::<Vec<_>>();
    if args.len() > 2 {
        println!("Usage: lox [script]");
        // Ref for exit codes:
        // https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
        exit(64);
    }
    if args.len() == 2 {
        println!("executing file: {}", &args[1]);
        match run_file(&args[1]) {
            Ok(()) => Ok(()),
            Err(_) => exit(65),
        }
    } else {
        println!("running prompt");
        run_prompt()
    }
}
