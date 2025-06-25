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

#ifndef DEQUE_H
#define DEQUE_H

#include "process.h"

typedef struct node Node;

struct node {
  Process *process;
  Node *prev;
  Node *next;
};

typedef struct deque {
  long size;
  Node *top;
  Node *bottom;
} Deque;

// Create a new empty Deque and return a pointer to it

Deque *new_deque();

// Free the memory associated with a Deque

void free_deque(Deque *deque);

// Add a Process to the top of a Deque

void deque_push(Deque *deque, Process* data);

// Add a Process to the bottom of a Deque

void deque_insert(Deque *deque, Process* data);

// Remove and return the top Process from a Deque

Process* deque_pop(Deque *deque);

// Remove and return the bottom Process from a Deque

Process* deque_remove(Deque *deque);

#endif
