/*
 * Problem 2 in Assignment 2
 * COMP20007 Design of Algorithms
 * Semester 1 2019
 *
 * Written by: Chan Jie Ho
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct node {

  int lowest_cost[2];
  int shortest;
  int origin[100];

} Node;

typedef struct edge {

  int source;
  int cost;
  int destination;

} Edge;

#define INFINITY 2147483647
#define START 0

/* --- DO NOT CHANGE THE CODE BELOW THIS LINE --- */

void problem_2_a();
void problem_2_b();
void print_usage_and_exit(char **argv);

int main(int argc, char **argv) {
  if (argc != 2) {
    print_usage_and_exit(argv);
  }

  /* If the argument provided is "a" then run problem_2_a(),
   * run problem_2_b() for "b", and fail otherwise. */
  if (strcmp(argv[1], "a") == 0) {
    problem_2_a();
  } else if (strcmp(argv[1], "b") == 0) {
    problem_2_b();
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

/* TODO: Implement your solution to Problem 2.a. in this function. */
void problem_2_a() {

  int i, n;

  scanf("%d", &n);
  Node nodes[n];

  for (i = 0 ; i < n ; i++) {

    nodes[i].lowest_cost[0] = INFINITY;
    nodes[i].origin[0] = -1;
  
  }

  nodes[0].lowest_cost[0] = 0;

  for (i = 0 ; i < n ; i++) {

    int edges, j;
    scanf("%d", &edges);

    for (j = 0 ; j < edges ; j++) {

      int next, cost;
      scanf("%d %d", &next ,&cost);

      if (nodes[i].lowest_cost[0] < INFINITY && nodes[next].lowest_cost[0] > (nodes[i].lowest_cost[0] + cost)) {

        nodes[next].lowest_cost[0] = nodes[i].lowest_cost[0] + cost;
        nodes[next].origin[0] = i;
      
      }      

    }

  }

  
  if (nodes[n-1].lowest_cost[0] == INFINITY) {

    printf("No Path");

  }

  else {

    int current = n-1, edges = 0;
    int path[100];

    printf("%d\n", nodes[n-1].lowest_cost[0]);

    while (current != 0) {
      path[edges] = current;
      edges++;
      current = nodes[current].origin[0];
    }

    printf("%d\n", edges);
    printf("%d\n", START);
    edges--;

    for (i = edges ; i >= 0 ; i--) {

      printf("%d\n", path[i]);

    }

  }

}

/* TODO: Implement your solution to Problem 2.b. in this function. */
void problem_2_b() {

  int i, n, k, edges = 0, j;
  Edge all_edges[10000];

  scanf("%d %d", &n, &k);
  Node nodes[n];

  // Initialise n nodes to have the lowest cost be infinity, and origin be null (-1)
  // Lowest cost is an array of size 2 – one to hold the lowest cost at the beginning of an iteration
  // and another to hold the lowest cost at the end of the iteration

  for (i = 0 ; i < n ; i++) {

    nodes[i].lowest_cost[0] = INFINITY;
    nodes[i].lowest_cost[1] = INFINITY;
    nodes[i].origin[0] = -1;
  
  }

  // Insert all the edges into an array

  for (i = 0 ; i < n ; i++) {

    int edge;
    scanf("%d", &edge);

    for (j = 0 ; j < edge ; j++) {

      int next, cost;
      scanf("%d %d", &next ,&cost);
      
      all_edges[edges + j].source = i;
      all_edges[edges + j].cost = cost;
      all_edges[edges + j].destination = next;

    }

    edges += edge;

  }

  // The source node would have a lowest cost of 0

  nodes[0].lowest_cost[0] = nodes[0].lowest_cost[1] = 0;

  // We first find the lowest cost of any path from the source that has at most 1 edge. From there, we can find the lowest cost
  // of paths with at most 2 edges using the path with 1 edge by iterating through all the edges connecting to that node connected
  // to the source node through the first iteration. Therefore we only need to do this iteration k-1 times to get the lowest cost
  // of a DAG path with at most k steps
  
  for (i = 0; i < k; i++) {

    // Iterate through all the edges

    for (j = 0 ; j < edges ; j++) {

      int source = all_edges[j].source;
      int dest = all_edges[j].destination;
      int cost = all_edges[j].cost;
      
      // Check if the source of the edge has been connected to the partially built path, then check if adding the edge would be 
      // lower than the lowest cost 
      if (nodes[source].lowest_cost[0] != INFINITY && nodes[source].lowest_cost[0] + cost < nodes[dest].lowest_cost[1]) {
        nodes[dest].lowest_cost[1] = nodes[source].lowest_cost[0] + cost;
        nodes[dest].origin[i+1] = source;

      }
      
    }

    // Update the cost after the iteration to be the cost at the beginning of the next iteration
    int m;
    for (m = 0 ; m < n ; m++) {

      if (nodes[m].lowest_cost[0] != nodes[m].lowest_cost[1]) {

        nodes[m].lowest_cost[0] = nodes[m].lowest_cost[1];
        nodes[m].shortest = i + 1;
        
      }
    }
    

  }

  
  if (nodes[n-1].lowest_cost[0] == INFINITY) {

    printf("No Path");

  }

  else {

    int path[100];
    int current = n-1;

    printf("%d\n", nodes[n-1].lowest_cost[0]);
    printf("%d\n", nodes[n-1].shortest);

    for (i = nodes[n-1].shortest ; i > 0 ; i--) {

      path[i] = nodes[current].origin[i];
      current = nodes[current].origin[i];
    
    }

    for (i = 1; i <= nodes[n-1].shortest ; i++) {

      printf("%d\n", path[i]);
      
    }
    
    printf("%d\n", n-1);

  }
  
  
}

