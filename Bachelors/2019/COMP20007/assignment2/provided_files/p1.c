/*
 * Problem 1 in Assignment 2
 * COMP20007 Design of Algorithms
 * Semester 1 2019
 *
 * Written by: Chan Jie Ho
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void siftDown(int heap[], int n, int parent);
void right_max_heapify(int heap[], int n, int parent);
int* swap(int* heap, int first, int second);

/* --- DO NOT CHANGE THE CODE BELOW THIS LINE --- */

void problem_1_a();
void problem_1_b();
void print_usage_and_exit(char **argv);

int main(int argc, char **argv) {
  if (argc != 2) {
    print_usage_and_exit(argv);
  }

  /* If the argument provided is "a" then run problem_1_a(),
   * run problem_1_b() for "b", and fail otherwise. */
  if (strcmp(argv[1], "a") == 0) {
    problem_1_a();
  } else if (strcmp(argv[1], "b") == 0) {
    problem_1_b();
  } else {
    print_usage_and_exit(argv);
  }

  return 0;
}

/* Print the usage information and exit the program. */
void print_usage_and_exit(char **argv) {
  fprintf(stderr, "usage: %s [ab]\n", argv[0]);
  exit(EXIT_FAILURE);
}

/* --- DO NOT CHANGE THE CODE ABOVE THIS LINE --- */

/* TODO: Implement your solution to Problem 1.a. in this function. */
void problem_1_a() {
  int n, i;

  scanf("%d", &n);
  int heap[n];

  // Put n integers into a heap
  for (i = 0 ; i < n ; i++) {
  
    scanf("%d", &heap[i]);
  
  }
  
  // Starting from second lowest row (at index n/2 - 1), we make sure each node satisfies the max-heap requirement
  for (i = n / 2 - 1 ; i >= 0 ; i--) {
  
    siftDown(heap, n, i); 
    
  }
  
  for (i = 0 ; i < n ; i++) {
  
    printf("%d\n", heap[i]);
  
  }

}

/* TODO: Implement your solution to Problem 1.b. in this function. */
void problem_1_b() {
  int n, i;

  scanf("%d", &n);
  int heap[n];

  for (i = 0 ; i < n ; i++) {
  
    scanf("%d", &heap[i]);
  
  }
  
  // Build heap
  for (i = n / 2 - 1 ; i >= 0 ; i--) {

    siftDown(heap, n, i); 
  
  }

  // Make the heap right-handed
  right_max_heapify(heap, n, 0);

  
  for (i = 0 ; i < n ; i++) {
    
    printf("%d\n", heap[i]);

  }
}


void siftDown(int heap[], int n, int parent) {

  int left, right, largest;

  left = 2 * parent + 1;
  right = 2 * parent + 2;


  // Find the largest element among the node (parent), and its two children (if they exist)
  if (left < n && heap[left] > heap[parent]) {

    largest = left;
  
  }
  else {
  
    largest = parent;
  
  }

  if (right < n && heap[right] > heap[largest]) {
  
    largest = right;
  
  }

  // The largest is not the parent (does not satisfy the max-heap requirement)
  if(largest != parent) {
  
    // Swap the largest with the element
    heap = swap(heap, parent, largest);

    // Check if the swapped node satisfies the max-heap requirement
    siftDown(heap, n, largest);

  }
  
}

void right_max_heapify(int heap[], int n, int parent) {

  int left, right;

  left = 2 * parent + 1;
  right = 2 * parent + 2;


  if (left < n) {

    // Already in a heap, so just need to swap left and right nodes
    if (heap[left] > heap[right]) {

      heap = swap(heap, left, right);  

      // Since the larger of the two will still satisfy max-heap requirement, we just check that
      // the smaller element satisfies the max-heap requirement after the swap
      siftDown(heap, n, left);

    }

    // Perform the function on the children
    right_max_heapify(heap, n, left);
    right_max_heapify(heap, n, right);

  }
  
}

int* swap(int* heap, int first, int second) {
  
  int temp = heap[first];
  heap[first] = heap[second];
  heap[second] = temp;
  
  return heap;

}