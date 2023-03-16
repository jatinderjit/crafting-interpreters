# 3. The Lox Language

```lox
// Your first Lox program!
print "Hello, world!";
```

- No parenthesis required because `print` is a built in statement.
- Comments start with `//`.
- Dynamic typing
- Automatic memory management: For lots more on this, see
  “[A Unified Theory of Garbage Collection](https://researcher.watson.ibm.com/researcher/files/us-bacon/Bacon04Unified.pdf)”
  (PDF) #todo

## Data types

- Booleans: `true`, `false` (Operators: `and`, `or`, `!`)
- Numbers: only double precision floating points (`1234`, `12.34`)
- Strings double quoted.
- Nil

## Expressions

- Infix operators: `a + b`
- Ternary (misfix): `condition ? then : else`
- `-` operator is both infix and prefix
- Standard comparison operators
- operators have the same precedence and associativity as C

### Statements

- Baking print into the language, instead of a core library, is a hack that
  allows our in-progress interpreter to start producing output before we’ve
  implemented all of the machinery required to define functions, look them up by
  name, and call them.
- An expression’s main job is to produce a _value_, a statement’s job is to
  produce an _effect_.
- An expression followed by a semicolon (`;`) promotes the expression to
  statement-hood. This is called an **expression statement**.
- If you want to pack a series of statements where a single one is expected, you
  can wrap them up in a block.

  ```lox
  {
      print "One statement.";
      print "Two statements.";
  }
  ```

- You declare variables using `var` statements. If you omit the initializer, the
  variable’s value defaults to `nil`.
- These are same as in C: if/else, while loops, and for loops.

### Functions

- A function call expression looks the same as it does in C.
- An **argument** is an actual value you pass to a function when you call it. So
  a function _call_ has an _argument_ list. Sometimes you hear **actual
  parameter** used for these.
- A **parameter** is a variable that holds the value of the argument inside the
  body of the function. Thus, a function _declaration_ has a _parameter_ list.
  Others call these **formal parameters** or simply **formals**.
- The body of a function is always a block. Inside it, you can return a value
  using a `return` statement.
- If execution reaches the end of the block without hitting a `return`, it
  implicitly returns `nil`.
- Functions are _first class_: they are real values that you can get a reference
  to, store in variables, pass around, etc.
- We can have **closures**: functions that _close over_ and hold on to the
  variables it needs.

### Classes

- There are two approaches to objects,
  [classes](https://en.wikipedia.org/wiki/Class-based_programming) and
  [prototypes](https://en.wikipedia.org/wiki/Prototype-based_programming). Lox
  uses classes.
- We could add some sort of `new` keyword, but to keep things simple, in Lox the
  class itself is a factory function for instances.
- `init` method is called automatically when an object is constructed.
- `<` operator is used for inheritance.
- Use `super` to call base class's methods (including for `init`).
- Lox is not a _pure_ object-oriented language - values of primitive types
  aren’t real objects in the sense of being instances of classes. They don’t
  have methods or properties.

## Challenges

### 1. Write Lox programs, find edge cases

> Write some sample Lox programs and run them (you can use the implementations
  of Lox in [my repository](https://github.com/munificent/craftinginterpreters)).
  Try to come up with edge case behavior I didn’t specify here. Does it do what
  you expect? Why or why not?

\#todo

### 2. Open questions regarding specifications

> This informal introduction leaves a _lot_ unspecified. List several open
   questions you have about the language’s syntax and semantics. What do you
   think the answers should be?

- Does not specify if multiple inheritance is supported. Don't think it will be
  supported, it will have design complications for attribute resolution.
- Does not specify if anonymous functions are supported.

### 3. Missing features

> Lox is a pretty tiny language. What features do you think it is missing that
  would make it annoying to use for real programs? (Aside from the standard
  library, of course.)

- No support for arrays / contiguous memory allocation (Though a custom class
  can be defined, but it cannot provide guarantees since we don't have private
  variables. Maybe super can be used for private variables?)
- Overriding operators
- Variadic arguments
