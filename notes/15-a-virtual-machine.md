# A Virtual Machine

- The interpreter will spend about 90% of its time inside `run()`.
- We'll implement it as a simple outer loop that goes on reading and executing a
  single bytecode instruction at a time.
- **Decoding** or **dispatching** is to figure out the code to be executed given
  a numeric opcode.
  - Some common techniques to do bytecode dispatch efficiently: "direct threaded
    code", "jump table" and "computed goto".
  - We'll just use a single giant switch statement, with a case for each opcode.

## C Macros

- Even operators (`+`, `-`, `*`, `/`, etc.) can be passed as an argument to a macro!
- It's all just text tokens for the preprocessor.

```c
#define BINARY_OP(a, b, op) (a op b)
int add = BINARY_OP(1, 2, +);
```

### Do-while trick

You have to be careful when defining a macro that expands to a series of
statements.

- If defined naively:

  ```c
  #define WAKE_UP() makeCoffee(); drinkCoffee();
  if (morning) WAKE_UP();
  // will expand to:
  if (morning) makeCoffee(); drinkCoffee();
  // `drinkCoffee` will always be called!
  ```

- If the blocks are used, a semi-colon at the end can result in a compiler error:

  ```c
  #define WAKE_UP() { makeCoffee(); drinkCoffee(); }
  if (morning) WAKE_UP();
  // expands to:
  if (morning) { makeCoffee(); drinkCoffee(); };
  ```

- `do-while` can handle semi-colons as well!

  ```c
  #define WAKE_UP() do { makeCoffee(); drinkCoffee(); } while(false)
  ```

## Challenges

### Bytecode sequences

> What bytecode instruction sequences would you generate for the following
> expressions:
>
> ```txt
> 1 * 2 + 3
> 1 + 2 * 3
> 3 - 2 - 1
> 1 + 2 * 3 - 4 / -5
> ```

(Remember that Lox does not have a syntax for negative number literals, so the
`-5` is negating the number 5.)

1. `1 * 2 + 3`:

   - `OP_CONSTANT 1`
   - `OP_CONSTANT 2`
   - `OP_MULTIPLY`
   - `OP_CONSTANT 3`
   - `OP_ADD`
   - `OP_RETURN`

2. `1 + 2 * 3`:

   - `OP_CONSTANT 1`
   - `OP_CONSTANT 2`
   - `OP_CONSTANT 3`
   - `OP_MULTIPLY`
   - `OP_ADD`
   - `OP_RETURN`

3. `3 - 2 - 1`

   - `OP_CONSTANT 3`
   - `OP_CONSTANT 2`
   - `OP_SUBTRACT`
   - `OP_CONSTANT 1`
   - `OP_SUBTRACT`
   - `OP_RETURN`

4. `1 + 2 * 3 - 4 / -5`
   - `OP_CONSTANT 1`
   - `OP_CONSTANT 2`
   - `OP_CONSTANT 3`
   - `OP_MULTIPLY`
   - `OP_ADD`
   - `OP_CONSTANT 4`
   - `OP_CONSTANT 5`
   - `OP_NEGATE`
   - `OP_DIVIDE`
   - `OP_SUBTRACT`
   - `OP_RETURN`
