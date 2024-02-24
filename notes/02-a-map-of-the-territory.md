# 2. A Map of the Territory

## The Parts of a Language

- Front end: Scanning, parsing and static analysis
- Middle end: Intermediate Representation, Optimization
- Front end: Code generation

### Scanning / Lexing / Lexical Analysis

A **scanner** (or **lexer**) takes a stream of characters andchunks them into a
series **tokens**, like `(`, `.`, `)`, `123`, `"hi!"`, `min`, etc.

The scanner usually discards whitespace and comments, leaving a clean sequence
of meaningful tokens.

> “Lexical” comes from the Greek root "lex", meaning "word".

### Parsing

The next step is **parsing** - where the syntax gets the grammar. A parser takes
the flat sequence of tokens and builds a tree structure that mirrors the nested
nature of the grammar. These trees have a couple of different names—**parse
tree** or **abstract syntax tree (AST)**—depending on how close to the bare
syntactic structure of the source language they are.

![Parsing](/assets/crafting_interpreters_parsing.png)

### Static analysis

The first two stages are pretty similar across all implementations. Now, the
individual characteristics of each language start coming into play.

The first bit of analysis that most languages do is called **binding** or
**resolution**. For each **identifier**, we find out where that name is defined
and wire the two together. This is where **scope** comes into play—the region of
source code where a certain name can be used to refer to a certain declaration.

If the language is statically typed, this is when we type check.

All this semantic insight that is visible to us from analysis needs to be stored
somewhere:

- Often, it gets stored right back as attributes on the syntax tree itself—extra
  fields in the nodes that aren’t initialized during parsing but get filled in
  later.
- A lookup table off to the side. Typically, the keys to this table are
  identifiers—names of variables and declarations. In that case, we call it a
  **symbol table** and the values it associates with each key tell us what that
  identifier refers to.
- The most powerful bookkeeping tool is to transform the tree into an entirely
  new data structure that more directly expresses the semantics of the code.

### Intermediate Representation

You can think of the compiler as a pipeline where each stage’s job is to
organize the data representing the user’s code in a way that makes the next
stage simpler to implement. The front end of the pipeline is specific to the
source language the program is written in. The back end is concerned with the
final architecture where the program will run.

In the middle, the code may be stored in some intermediate representation (IR)
that isn’t tightly tied to either the source or destination forms (hence
“intermediate”). Instead, the IR acts as an interface between these two
languages.

There are a few well-established styles of IRs:

- control flow graph #todo
- static single-assignment #todo
- continuation-passing style #todo
- three-address code #todo

This lets you support multiple source languages and target platforms with less
effort. Say you want to implement Pascal, C, and Fortran compilers, and you want
to target x86, ARM, and, I dunno, SPARC. Normally, that means you’re signing up
to write nine full compilers: Pascal→x86, C→ARM, and every other combination.

A shared intermediate representation reduces that dramatically. You write one
front end for each source language that produces the IR. Then one back end for
each target architecture. Now you can mix and match those to get every
combination.

### Optimization

Once we understand what the user’s program means, we are free to swap it out
with a different program that has the same semantics but implements them more
efficiently—we can optimize it.

Some keywords to get you started are “constant folding”, “constant propagation”,
“common subexpression elimination”, “loop invariant code motion”, “global value
numbering”, “strength reduction”, “scalar replacement of aggregates”, “dead code
elimination”, and “loop unrolling”. #todo

### Code generation

The last step is converting it to a form the machine can actually run. In other
words, **generating code** (or **code gen**), where “code” here usually refers
to the kind of primitive assembly-like instructions a CPU runs.

We have a decision to make. Do we generate instructions for a real CPU or a
virtual one?

- If we generate real machine code, we get an executable that the OS can load
  directly onto the chip. Native code is lightning fast, but generating it is a
  lot of work. Speaking the chip’s language also means your compiler is tied to
  a specific architecture. If your compiler
  targets [x86](https://en.wikipedia.org/wiki/X86) machine code, it’s not going
  to run on an [ARM](https://en.wikipedia.org/wiki/ARM_architecture) device.

- To get around that, hackers like Martin Richards and Niklaus Wirth, of BCPL
  and Pascal fame, respectively, made their compilers produce _virtual_ machine
  code. Instead of instructions for some real chip, they produced code for a
  hypothetical, idealized machine. Wirth called this **p-code** for _portable_,
  but today, we generally call it **bytecode** because each instruction is often
  a single byte long.

- These synthetic instructions are designed to map a little more closely to the
  language’s semantics, and not be so tied to the peculiarities of any one
  computer architecture and its accumulated historical cruft.

### Virtual Machine

If your compiler produces bytecode, it’s your job to translate. Again, you have
two options. You can write a little mini-compiler for each target architecture
that converts the bytecode to native code for that machine. You’re basically
using your bytecode as an intermediate representation. The farther down the
pipeline you push the architecture-specific work, the more of the earlier phases
you can share across architectures.

There is a tension, though. Many optimizations, like register allocation and
instruction selection #todo, work best when they know the strengths and
capabilities of a specific chip. Figuring out which parts of your compiler can
be shared and which should be target-specific is an art.

Or you can write a **virtual machine** (**VM**), a program that emulates a
hypothetical chip supporting your virtual architecture at runtime. Running
bytecode in a VM is slower because every instruction must be simulated at
runtime each time it executes. In return, you get simplicity and portability.
Implement your VM in, say, C, and you can run your language on any platform that
has a C compiler.

### Runtime

For all but the basest of low-level languages, we usually need some services
that our language provides while the program is running. For example, garbage
collector. If our language supports “instance of” tests so you can see what kind
of object you have, then we need some representation to keep track of the type
of each object during execution.

In a fully compiled language, the code implementing the runtime gets inserted
directly into the resulting executable

### Shortcuts and Alternate Routes

#### Single-pass Compilers

Some simple compilers interleave parsing, analysis, and code generation so that
they produce output code directly in the parser, without ever allocating any
syntax trees or other IRs. These single-pass compilers restrict the design of
the language. You have no intermediate data structures to store global
information about the program, and you don’t revisit any previously parsed part
of the code. That means as soon as you see some expression, you need to know
enough to correctly compile it.

Pascal and C were designed around this limitation. At the time, memory was so
precious that a compiler might not even be able to hold an entire source file in
memory, much less the whole program. This is why Pascal’s grammar requires type
declarations to appear first in a block. It’s why in C you can’t call a function
above the code that defines it unless you have an explicit forward declaration
that tells the compiler what it needs to know to generate code for a call to the
later function.

**[Syntax-directed translation](https://en.wikipedia.org/wiki/Syntax-directed_translation)**
is a structured technique for building these all-at-once compilers. You
associate an _action_ with each piece of the grammar, usually one that generates
output code. Then, whenever the parser matches that chunk of syntax, it executes
the action, building up the target code one rule at a time.

#### Tree-walk interpreters

Some programming languages begin executing code right after parsing it to an AST
(with maybe a bit of static analysis applied). To run the program, the
interpreter traverses the syntax tree one branch and leaf at a time, evaluating
each node as it goes.

This implementation style is common for student projects and little languages,
but is not widely used for general-purpose languages since it tends to be slow.
Some people use “interpreter” to mean only these kinds of implementations, but
others define that word more generally.

#### Transpiler

Writing a complete back end for a language can be a lot of work. You write a
front end for your language. Then, in the back end, you produce a string of
valid source code for some other language that’s about as high level as yours.
Then, you use the existing compilation tools for that language as your escape
route.

They used call this a **source-to-source compiler** or a **transcompiler**,
before lots of languages started compiling to javascript.

#### Just-in-time compilation

The fastest way to execute code is by compiling it to machine code, but you
might not know what architecture your end user’s machine supports. The most
sophisticated JITs insert profiling hooks into the generated code to see which
regions are most performance critical and what kind of data is flowing through
them. Then, over time, they will automatically recompile those hot spots with
more advanced optimizations.

This is the same thing that the HotSpot Java Virtual Machine (JVM), Microsoft’s
Common Language Runtime (CLR), and most JavaScript interpreters do.

## Challenges

### 1. Open Source examples

> Pick an open source implementation of a language you like. Download the source
> code and poke around in it. Try to find the code that implements the scanner
> and parser. Are they handwritten, or generated using tools like Lex and Yacc?
> (.l or .y files usually imply the latter.)

Hand-written scanners/parsers:

- [Go](https://github.com/golang/go/blob/master/src/go/scanner/scanner.go).
- [Python](https://github.com/python/cpython/blob/main/Parser/parser.c)
- [Rust](https://github.com/rust-lang/rust/tree/master/compiler/rustc_parse/src)
- [v8](https://github.com/v8/v8/tree/main/src/parsing) (JavaScript)

Generated parsers:

- [RustPython](https://github.com/RustPython/RustPython/blob/main/compiler/parser/python.lalrpop)
  uses [LALROP](https://github.com/lalrpop/lalrpop)

### 2. Why not JIT?

> Just-in-time compilation tends to be the fastest way to implement dynamically
> typed languages, but not all of them use it. What reasons are there to not
> JIT?

- Complexity of writing a JIT.
- Implementations are tied to specific CPU architectures.
- JIT often needs to profile the code during the runtime, that will add memory
  overheads. This might matter more in embedded devices where memory may matter
  more than speed.
- If the code isn't meant to be run for long time (continuously), it might not
  be worth adding JIT.
- Some platforms (iOS, game consoles) disallow execution of code generated at
  runtime. The OS won't allow it.

### 3. Lisp and C

> Most Lisp implementations that compile to C also contain an interpreter that
> lets them execute Lisp code on the fly as well. Why?

~~Probably REPL?~~

Most Lisps support macros, which can be evaluated at the runtime.
