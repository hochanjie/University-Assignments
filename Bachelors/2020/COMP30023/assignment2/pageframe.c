/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * Pageframe data structure for Assignment 2
 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#include <strings.h>

#include "pageframe.h"

#define FREE -1
#define EMPTY 0
#define START 0


// Assumes that the set_of_pages doesn't have the chunk to be added
void add_block(PageFrame* set_of_pages, Block* chunk){
  Block* block = (Block*)malloc(sizeof(*block));
  
  set_of_pages -> free_space += chunk -> length;
  block = set_of_pages -> bottom;
  // first block to be added
  if (block == NULL) {
    set_of_pages -> bottom = chunk;
  }
  else{
    Block* before = (Block*)malloc(sizeof(*before));
    // add to the bottom of the set
    if (block -> start > chunk ->start) {
      chunk ->next = block;
      block ->prev = chunk;
      set_of_pages -> bottom = chunk;
      merge_frame(set_of_pages);
    }
    else {
      before = block;
      block = block -> next;

      // find place to insert it
      while (block && block -> start <= chunk ->start) {
        before = block;
        block = block -> next;
      }


      before -> next = chunk;
      chunk -> prev = before;
      chunk -> next = block;
      
      // not the top
      if (block) {
        block -> prev = chunk;
      }
      // new top
      else {
        set_of_pages -> top = chunk;
      }
      merge_frame(set_of_pages);
      
    }
  }
}

void remove_all(PageFrame* frame, long process_id) {
  Block* curr = frame -> bottom;
  while (curr) {
    if (curr -> owner_id == process_id) {
      curr -> owner_id = FREE;
      frame -> free_space += curr -> length;

    }
    curr = curr -> next;
  }
  
}

long calculate_free(PageFrame* frame) {
  Block* curr = frame -> bottom;
  long free = 0;
  while (curr) {
    if (curr -> owner_id == FREE) {
      free += curr -> length;
    }
    curr = curr -> next;
  }
  return free;
}

PageFrame *new_page_frame(long memory){
  PageFrame *frame = (PageFrame*)malloc(sizeof(*frame));
  Block *new = (Block*)malloc(sizeof(*new));
  assert(new && frame);

  new -> length = memory;
  new -> prev = new -> next = NULL;
  new -> owner_id = FREE;
  new -> start = START;

  if (memory != -1) {
    frame -> top = frame -> bottom = new;
    frame -> free_space = memory;
  }
  else {
    frame -> top = frame -> bottom = NULL;
    frame -> free_space = EMPTY;
  }
  return frame;
}


void merge_frame(PageFrame* frame) {
  Block* curr = frame -> bottom;
  long counter = 0;

  if (curr -> next) {
    
    Block* next = curr -> next;
    while (next) {
      if (curr -> start + curr -> length == next -> start && curr -> owner_id == next -> owner_id){
     
        curr -> length += next -> length;
        curr -> next = next -> next;
       
      }

      curr = next;
      next = next -> next;
    }
  }
}


Block* find_free(PageFrame *frame, long required, char* scheduling_algorithm) {

  long minimum = required;

  if (strcmp(scheduling_algorithm, "v") == 0){
    minimum = 1;
  }

  Block * curr = frame -> bottom;

  while (curr){

    if (curr -> owner_id == FREE && curr -> length >= minimum) {
      return curr;
    }
    curr = curr -> next;
  } 
  return NULL;

}

void remove_free(PageFrame* set_of_pages) {
  Block* curr = (Block*)malloc(sizeof(*curr));
  curr = set_of_pages -> bottom;

  while (curr && curr -> owner_id == FREE) {
    curr = curr -> next;
  }
  if (curr) {
    set_of_pages -> bottom = curr;
  }
  else {
    set_of_pages -> bottom = set_of_pages -> top = NULL;
    set_of_pages -> free_space = EMPTY;
  }

}


void free_page_frame(PageFrame *frame) {

  Block *curr = (Block*)malloc(sizeof(*curr));
  Block *prev = (Block*)malloc(sizeof(*prev));
	
  assert(frame && curr && prev);

  if (frame -> bottom){
    curr = frame -> bottom;
  
    while (curr) {
      prev = curr;
      curr = curr -> next;
      free(prev);
    }
  }

  else {
    prev = frame -> top;
    
    while (prev) {
      curr = prev;
      prev = prev -> prev;
      free(curr);
    }
  }
	
  free(frame);
  frame = NULL;
}

void print_pages(PageFrame *pages) {
  long set_of_pages[pages -> free_space];
  long i=0, j;
  Block* curr = pages -> bottom;
  
  if (pages -> free_space != 0) { 
    while (curr) {

      for (j =0; j < curr -> length; j++) {
        
        set_of_pages[i+j] = curr -> start + j;
      }
      i += j;
      curr = curr -> next;
    }

    j = 0;
    printf("[%li", set_of_pages[j]);
    for (j=1; j < i ; j++) {
      printf(",%li", set_of_pages[j]);
    }
    printf("]");
  }
  else {
    printf("[]");
  }
}