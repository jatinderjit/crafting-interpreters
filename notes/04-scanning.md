# Scanning

- The scanner takes a _raw_ source code as a series of characters and emits
  **tokens**.
- Tokens are the meaningful "words" and "punctuation" that make up the
  language's grammar.
- Also referred as "lexing", which is short for "lexical analysis". Earlier
  scanning meant to code that dealt with reading raw source code from disk and
  buffering them in memory. Lexing used to be the subsequent phase.

The name REPL comes from Lisp where implementing is as simple as:

```cl
(print (eval (read)))
```

## Lexemes and Tokens

- In lexical analysis, we scan through the list of characters and group them
  together. Each of these blobs of characters is called a **lexeme**.
- For a line of code: `var language = "lox";`, the lexemes would be `var`,
  `language`, `=`, `"lox"` and `;`.
- Each keyword, operator, bit of punctuation and literal type has a different
  type, which we refer to here as Token Type.
- The parser could categorize tokens from the raw lexemes. Instead, we do that
  when we recognize a lexeme.
- In case of literal values, the scanner will also convert the textual
  representation of a value to the runtime object that will be later used by the
  interpreter.
- We will also track the token locations. This will be helpful during error
  handling.

## Challenges

### Not Regular

> The lexical grammars of Python and Haskell are not regular. What does that
> mean, and why aren’t they?

A regular grammar should only need to store a single finite number identifying
which state it is in.

Both Python and Haskell have significant indentation. The scanner has to
maintain state to track the indentation values, which means it needs extra
memory. The language is no longer regular.

These can be made regular, if we limit the possible indentations and enumerate
them.

### Meaningful spaces

> Aside from separating tokens—distinguishing `print foo` from
> `printfoo`—spaces aren’t used for much in most languages. However, in a couple
> of dark corners, a space does affect how code is parsed in CoffeeScript, Ruby,
> and the C preprocessor. Where and what effect does it have in each of those
> languages?

\#todo
