# A Virtual Machine

- The interpreter will spend about 90% of its time inside `run()`.
- We'll implement it as a simple outer loop that goes on reading and executing a
  single bytecode instruction at a time.
- **Decoding** or **dispatching** is to figure out the code to be executed given
  a numeric opcode.
  - Some common techniques to do bytecode dispatch efficiently: "direct threaded
    code", "jump table" and "computed goto".
  - We'll just use a single giant switch statement, with a case for each opcode.
