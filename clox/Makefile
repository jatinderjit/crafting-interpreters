CC = gcc
CFLAGS = -Wall -Wextra -g -pedantic -std=c99 -O2

OUT = lox

# Define all the source and header files
SRCS = $(wildcard *.c)
HDRS = $(wildcard *.h)

# Define all the object files
OBJECTS = $(SRCS:.c=.o)

# Rule to compile a specific source file
.PHONY: %.o
%.o: %.c $(HDRS)
	$(CC) $(CFLAGS) -c $< -o $@

# Rule to link the final executable
.PHONY: all
all: $(OUT)

$(OUT): $(OBJECTS)
	$(CC) $(CFLAGS) $(LDFLAGS) $^ -o $@

# Rule to clean all object files and the executable
.PHONY: clean
clean:
	rm -f $(OBJECTS) $(OUT)

# Phony target for automatic dependency generation
.PHONY: depend
depend: .depend

# Generate dependency information
.depend: $(SRCS) $(HDRS)
	@rm -f .depend
	@$(CC) $(CFLAGS) -M $(SRCS) | sed 's/\.o\.s*://' | grep -v '^$$' >> .depend

-include .depend
