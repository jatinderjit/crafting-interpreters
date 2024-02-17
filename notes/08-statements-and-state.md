# Statements and State

Our interpreter will now _remember_. If a variable is defined, the interpreter
will hold on to the value of the variable. It will need an internal state.

## Statements

- Statements, by definition, don't evaluate to a value.
- They produce side effects. It could mean producing user-visible output or
  modifying some state in the interpreter.

Extend Lox's grammar with two simplest kinds of statements:

1. An **expression statement** lets you place an expression where a statement is
  expected.
2. A **print statement** evaluates an expression and displays the result to the
  user.

Variable declarations are statements, but they won't be allowed everywhere.

```lox
if (cond) print "something"; // OK
if (cond) var name = "something"; // Not OK
```

We _could_ allow the latter, but it's confusing. What is the scope of the `name`
variable?

To accommodate this, we'll split the statement grammar in two.

New rules:

```bnf
program        → statement* EOF ;

declaration    → varDecl
               | statement ;

statement      → exprStmt
               | printStmt ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;
```

## Environments

- Environment is the data structure that stores variables names and values.
- We'll use Hashmap.
- Use raw string as keys instead of tokens to ensure all those tokens refer to
  the same map key.

**Design choice:** when adding a key to the map, we don't check if it's already
present. This means we allow _re_-definition of variables.

We could choose to make an error instead. It would be helpful if the user didn't
intend to redefine. But that interacts poorly with the REPL.

**Design choice:** What to do if variable is not found (when reading)?
- Make it a syntax error
- Make it a runtime error
- Allow it and return some default value like `nil`

We can't make it a static error to _mention_ a variable before it's declared. It
becomes harder to define mutually recursive functions. (We can mandate
declaration of functions, that can solve this).

We choose to report a runtime error.
