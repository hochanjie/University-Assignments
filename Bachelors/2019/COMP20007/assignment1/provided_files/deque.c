/* * * * * * *
 * Deque module (i.e., double ended queue) for Assignment 1
 *
 * created for COMP20007 Design of Algorithms 2019
 * template by Tobias Edwards <tobias.edwards@unimelb.edu.au>
 * implementation by Chan Jie Ho
 */

//                   WRITE YOUR IMPLEMENTATION HERE
//
// You should fill in the function definitions for
//  - new_deque()
//  - free_deque()
//  - deque_push()
//  - deque_insert()
//  - deque_pop()
//  - deque_remove()
//  - deque_size()
//
// Don't be shy to add any extra functions or types you may need.

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "deque.h"
#include "point.h"

#define EMPTY 0
#define NEW 1
#define NON_EMPTY 1

// Moved struct definitions to deque.h

// Create a new empty Deque and return a pointer to it
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Deque *new_deque() {
  // TODO: Implement new_deque()

  Deque *deque = (Deque*)malloc(sizeof(*deque));
  assert(deque);

  deque -> top = deque -> bottom = NULL;
  deque -> size = EMPTY;

  return deque;
}


// Free the memory associated with a Deque
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void free_deque(Deque *deque) {
  // TODO: Implement free_deque()

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
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void deque_push(Deque *deque, Point data) {
  // TODO: Implement deque_push()

  Node *new = (Node*)malloc(sizeof(*new));
  assert(new && deque);
	
	new -> data = data;
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

// Add a Point to the bottom of a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void deque_insert(Deque *deque, Point data) {
  // TODO: Implement deque_insert()

  Node *new = (Node*)malloc(sizeof(*new));
  assert(new && deque);
	
	new -> data = data;
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

// Remove and return the top Point from a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Point deque_pop(Deque *deque) {
  // TODO: Implement deque_pop()

  Node *curr = deque -> top;
  Point popped = deque -> top -> data;

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

// Remove and return the bottom Point from a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Point deque_remove(Deque *deque) {
  // TODO: Implement deque_remove()
  
  Node *curr = deque -> bottom;
  Point popped = deque -> bottom -> data;

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

// Return the number of Points in a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
int deque_size(Deque *deque) {
  // TODO: Implement deque_size()
  return deque -> size;
}

// TODO: Add any other functions you might need for your Deque module
