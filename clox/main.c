#include "chunk.h"
#include "vm.h"

int main(void) {
  initVM();

  Chunk chunk;
  initChunk(&chunk);

  /* -((1.2 + 3.4) / 5.6)
   *       `-`
   *        |
   *       `/`
   *      /  \
   *    `+`  5.6
   *    / \
   *  1.2  3.4
   */
  int constant = addConstant(&chunk, 1.2);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  constant = addConstant(&chunk, 3.4);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  writeChunk(&chunk, OP_ADD, 123);

  constant = addConstant(&chunk, 5.6);
  writeChunk(&chunk, OP_CONSTANT, 123);
  writeChunk(&chunk, constant, 123);

  writeChunk(&chunk, OP_DIVIDE, 123);

  writeChunk(&chunk, OP_NEGATE, 123);

  writeChunk(&chunk, OP_RETURN, 123);

  interpret(&chunk);

  freeVM();
  freeChunk(&chunk);
  return 0;
}
