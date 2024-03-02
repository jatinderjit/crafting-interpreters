# Chunks of Bytecode

> If you find that you’re spending almost all your time on theory, start
> turning some attention to practical things; it will improve your theories.
> If you find that you’re spending almost all your time on practice, start
> turning some attention to theoretical things; it will improve your practice.
> — Donald Knuth

## Walking the AST

Advantages:

- Simple to implement. The runtime representation of the code directly maps to
  the syntax.
- Portable. Will run on any platform that supports the language it is
  implemented in.

Disadvantages:

- Not memory-efficient. Each piece of syntax becomes an AST node with lots of
  pointers between them. Each of these pointers add an extra 32/64 bits of
  overhead.
- Sprinkling data across the heap does not take any advantage of _spacial
  locality_.

## Compile to native code

- Machine code. The fastest way.
- But the performance comes at a cost. It ain't easy!
- Most chips have sprawling architectures with instructions accreted over
  decades.
- Though a well-architected compiler could share the front end and most of the
  middle layer of optimization passes. It's mainly the code generation that
  needs to be written afresh for each architecture. And
  [LLVM](https://llvm.org/) can help with that too!

## Bytecode

- Bytecode sits in the middle (of a tree-walk interpreter and machine code).
  - Retains the portability of a tree-walker.
  - Sacrifices some simplicity to get a performance boost (though not as
    fast as fully native).
- Structurally resembles machine code - dense, linear sequence of binary
  instructions that keep overheads low and play nice with the cache.
- However, it's much simpler, higher-level instruction set than any real chip.
- Executed over an _emulator_ - a simulated chip that interprets bytecode.
  Also called a _virtual machine (VM)_.
