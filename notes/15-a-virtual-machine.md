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
