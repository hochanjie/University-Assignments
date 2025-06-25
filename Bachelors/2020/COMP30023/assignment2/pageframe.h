/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * PageFrame data structure for Assignment 2
 */

#ifndef PAGEFRAME_H
#define PAGEFRAME_H

typedef struct block Block;

struct block {
  Block* prev;
  long owner_id;
  long start;
  long length;
  Block* next;
};

typedef struct pageframe {
  long free_space;
  Block *top;
  Block *bottom;
} PageFrame;

void add_block(PageFrame* set_of_pages, Block* chunk);

Block* find_free(PageFrame* frame, long required, char* scheduling_algorithm);

void remove_free(PageFrame* set_of_pages);

void remove_all(PageFrame* frame, long process_id);

long calculate_free(PageFrame* frame);

PageFrame *new_page_frame(long memory);

void merge_frame(PageFrame* frame);

void free_page_frame(PageFrame *frame);

void print_pages(PageFrame *pages);

#endif