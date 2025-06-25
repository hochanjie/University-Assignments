CC=gcc
CFLAGS= -lm -Wextra -std=gnu99 -I -Wall. 
DEPS = 
OBJ = scheduler.o process.o pageframe.o deque.o
EXE = scheduler

##Create .o files from .c files. Searches for .c files with same .o names given in OBJ
%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

##Create executable linked file from object files. 
$(EXE): $(OBJ)
	gcc -o $@ $^ $(CFLAGS)

##Delete object files
clean:
	/bin/rm $(OBJ)

##Performs clean (i.e. delete object files) and deletes executable
clobber: clean
	/bin/rm $(EXE) 
