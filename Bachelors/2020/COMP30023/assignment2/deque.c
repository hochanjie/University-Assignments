/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * Deque module (i.e., double ended queue) for Assignment 2
 *
 * adapted from the work I implemented for Assignment 1 COMP20007 Design of Algorithms 2019
 * template by Tobias Edwards <tobias.edwards@unimelb.edu.au>
 * implementation by Chan Jie Ho
 */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "deque.h"
#include "process.h"

#define EMPTY 0
#define NEW 1
#define NON_EMPTY 1

// Moved struct definitions to deque.h

// Create a new empty Deque and return a pointer to it

Deque *new_deque(){

  Deque *deque = (Deque*)malloc(sizeof(*deque));
  assert(deque);

  deque -> top = deque -> bottom = NULL;
  deque -> size = EMPTY;

  return deque;
}


// Free the memory associated with a Deque

void free_deque(Deque *deque) {

  Node *curr = (Node*)malloc(sizeof(*curr));
  Node *prev = (Node*)malloc(sizeof(*prev));
	
  assert(deque && curr && prev);

  if (deque -> bottom){
    curr = deque -> bottom;
  
    while (curr) {
      prev = curr;
      curr = curr -> next;
      free(prev);
    }
  }

  else {
    prev = deque -> top;
    
    while (prev) {
      curr = prev;
      prev = prev -> prev;
      free(curr);
    }
  }
	
  free(deque);
  deque = NULL;
}

// Add a Point to the top of a Deque

void deque_push(Deque *deque, Process* process) {

  Node *new = (Node*)malloc(sizeof(*new));
  assert(new && deque);
	
	new -> process = process;
  new -> next = NULL;
  new -> prev = deque -> top;

  if (deque -> top) {
    deque -> top -> next = new;
  }

  deque -> top = new;

  if (deque -> bottom == NULL) {
		deque -> bottom = new;
	}

  deque -> size += NEW;	
}

// Add a Process to the bottom of a Deque

void deque_insert(Deque *deque, Process* process) {

  Node *new = (Node*)malloc(sizeof(*new));
  assert(new && deque);
	
	new -> process = process;
  new -> prev = NULL;
  new -> next = deque -> bottom;

  if (deque -> bottom){
    deque -> bottom -> prev = new;
  }

	deque -> bottom = new;

  if (deque -> top == NULL) {
		deque -> top = new;
	}

  deque -> size += NEW;
}

// Remove and return the top Process from a Deque

Process* deque_pop(Deque *deque) {

  Node *curr = deque -> top;
  Process* popped = deque -> top -> process;

  deque -> top = deque -> top -> prev;

  if (deque -> size > NON_EMPTY) {
    deque -> top -> next = NULL;
  }
  else {
    deque -> bottom = deque -> top;
  }
  
  
  free(curr);
  deque -> size -= NEW;

  return popped;
}

// Remove and return the bottom Process from a Deque

Process* deque_remove(Deque *deque) {
  
  Node *curr = deque -> bottom;
  Process* popped = deque -> bottom -> process;

  deque -> bottom = deque -> bottom -> next;

  if (deque -> size > NON_EMPTY) {
    deque -> bottom -> prev = NULL;
  }
  else {
    deque -> top = deque -> bottom;
  }
  
  free(curr);
  deque -> size -= NEW;

  return popped;
}
