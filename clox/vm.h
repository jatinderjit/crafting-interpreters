#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"
#include "value.h"

#define STACK_MAX 256

typedef struct {
  Chunk *chunk;

  // Instruction Pointer
  uint8_t *ip;

  Value stack[STACK_MAX];
  // stackTop points to where the next value to be pushed will go.
  Value *stackTop;
} VM;

typedef enum {
  INTERPRET_OK,
  INTERPRET_COMPILE_ERROR,
  INTERPRET_RUNTIME_ERROR
} InterpretResult;

void initVM(void);
void freeVM(void);

InterpretResult interpret(Chunk *chunk);

void push(Value value);
Value pop();

#endif
