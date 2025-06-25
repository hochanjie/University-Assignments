/* * * * * * *
 * Deque module (i.e., double ended queue) for Assignment 1
 *
 * created for COMP20007 Design of Algorithms 2019
 * template by Tobias Edwards <tobias.edwards@unimelb.edu.au>
 * implementation by <Insert Name Here>
 */

// You must not change any of the code already provided in this file, such as
// type definitions, constants or functions.
//
// You may, however, add additional functions and/or types which you may need
// while implementing your algorithms and data structures.

#ifndef DEQUE_H
#define DEQUE_H

#include "point.h"

typedef struct node Node;

struct node {
  Node *prev;
  Point data;
  Node *next;
};

// TODO: Fill in this struct definition, or change the typedef in deque.h
typedef struct deque {
  // TODO: Add fields here
  int size;
  Node *top;
  Node *bottom;
} Deque;

// Create a new empty Deque and return a pointer to it
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Deque *new_deque();

// Free the memory associated with a Deque
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void free_deque(Deque *deque);

// Add a Point to the top of a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void deque_push(Deque *deque, Point data);

// Add a Point to the bottom of a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
void deque_insert(Deque *deque, Point data);

// Remove and return the top Point from a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Point deque_pop(Deque *deque);

// Remove and return the bottom Point from a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
Point deque_remove(Deque *deque);

// Return the number of Points in a Deque
//
// TODO: Fill in the runtime of this function
// Runtime: O(1)
//
// DO NOT CHANGE THIS FUNCTION SIGNATURE
int deque_size(Deque *deque);

// TODO: Add any other functions you might need for your Deque module

#endif
