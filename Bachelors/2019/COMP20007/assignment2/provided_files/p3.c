/*
 * Problem 3 in Assignment 2
 * COMP20007 Design of Algorithms
 * Semester 1 2019
 *
 * Written by: Chan Jie Ho
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#define RIGHT -2
#define LEFT 2

typedef struct node Node;

struct node {
  Node *left;
  int value;
  Node *right;
};

Node* insert(Node *root, Node *new, int * inserted);
Node* balancedBST(int nodes[], int start, int end);
int max_height(Node* node);
void inOrder(Node* root, int array[], int *i);
void levelOrder(Node* root, int level);
void count_lines(Node *root, int level, int *lines);

/* --- DO NOT CHANGE THE CODE BELOW THIS LINE --- */

void problem_3();

int main(int argc, char **argv) {
  problem_3();
  return 0;
}

/* --- DO NOT CHANGE THE CODE ABOVE THIS LINE --- */

/* TODO: Implement your solution to Problem 3 in this function. */
void problem_3() {

  int n, i, inserted = 0;
  Node *root = NULL;

  scanf("%d", &n);
  

  for (i = 0 ; i < n ; i++) {

    int value;
    scanf("%d", &value);

    Node *new;

    /* make the new node */
    new = (Node*) malloc(sizeof(*new));
    assert(new!=NULL);

    new -> value = value;
    new -> left = NULL;
    new -> right = NULL;

    /* and insert it into the tree */
    root = insert(root, new, &inserted);
    
  }

  int height = max_height(root);
  printf("%d\n%d\n", inserted, height);

  i = 0;
  int nodes[inserted];
  inOrder(root, nodes, &i);

  n = sizeof(nodes) / sizeof(nodes[0]);
  Node* tree = balancedBST(nodes, 0, n-1);
  
  height = max_height(tree);
  int lines = 0;

  for (i = 1; i <= height ; i++) {

    count_lines(tree, i, &lines);

  }

  printf("%d\n", lines);

  for (i = 1; i <= height ; i++) {

    levelOrder(tree, i);

  }

}


Node* insert(Node *root, Node *new, int *inserted) {
	if (root == NULL) {

    *inserted += 1;
		return new;
	
  } 

  if (new->value == root->value) {
  
    free(new);
    return root;
  
  }
  else if (new->value < root->value) {
  
    root -> left = insert(root -> left, new, inserted);
	
  } 
  else {
	
  	root -> right = insert(root -> right, new, inserted);
	
  }
  
  return root;

}


int max_height(Node* node) {

  if (node == NULL) {

    return 0;
  
  }
  else {  

    /* compute the depth of each subtree */
    int left = max_height(node->left);
    int right = max_height(node->right);
  
    /* use the larger one */
    if (left > right) {

      return(left + 1);
    
    }
    else {
    
      return (right + 1);
    
    }
  
  }  

}  

Node* balancedBST(int nodes[], int first, int end) 
{ 
    int middle = (first + end)/2; 
    
    if (first > end) {

      return NULL; 
    
    }

    Node *new;

    /* make the new node */
    new = (Node*) malloc(sizeof(*new));
    assert(new!=NULL);

    new -> value = nodes[middle];
    new->left  = balancedBST(nodes, first, middle-1); 
    new->right = balancedBST(nodes, middle+1, end); 
    
    return new; 

} 



void inOrder(Node* root, int array[], int *i){

  if (root != NULL) {

    inOrder(root->left, array, i);

    array[*i]= root -> value;
    *i += 1;

    inOrder(root->right, array, i);

  }

}

void levelOrder(Node* root, int level) {
  if (root == NULL) {

    printf("-1\n");
    return;

  }

  if (level == 1) {

    printf("%d\n", root -> value);
        
  }
  else if (level > 1) {

    levelOrder(root->left, level-1);
    levelOrder(root->right, level-1);

  }

}

void count_lines(Node *root, int level, int *lines){

    if(root == NULL){
    
        *lines += 1;
        return;
    
    }

    if(level == 1){
    
        *lines += 1;
    
    }
    else if(level > 1){
    
        count_lines(root->left, level-1, lines);
        count_lines(root->right,level-1, lines);
    
    }

}