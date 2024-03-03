#ifndef clox_memory_h
#define clox_memory_h

#include "common.h"

// Double the capacity. If zero, set to 8.
#define GROW_CAPACITY(capacity) ((capacity) < 8 ? 8 : (capacity) * 2)

#define GROW_ARRAY(type, pointer, oldCount, newCount)                          \
  (type *)reallocate(pointer, sizeof(type) * (oldCount),                       \
                     sizeof(type) * (newCount))

#define FREE_ARRAY(type, pointer, oldCount)                                    \
  reallocate(pointer, sizeof(pointer) * (oldCount), 0)

void *reallocate(void *pointer, size_t oldSize, size_t newSize);

#endif
