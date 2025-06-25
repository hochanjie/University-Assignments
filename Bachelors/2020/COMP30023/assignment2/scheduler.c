/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * Main scheduler file that defines a scheduling algorithm as well as some memory 
 * allocation algorithms – Namely: First In First Out, Round Robin, and Shortest 
 * Job First for the scheduling algorithms; Swapping-X, Virtual Memory, and Optimal
 * Allocation for the memory allocation algorithms
 * 
 */

/* ========================================================================== */

/* Libraries to include and hash-defined variables */
/* ----------------------------------------------- */

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <assert.h>

#include "process.h"
#include "deque.h"
#include "pageframe.h"

#define PAGE 4
#define PAGE_FAULT 1
#define QUANTUM 10
#define START 0
#define FINISHED 0
#define LOAD_TIME 2
#define EMPTY 0
#define NEW 1
#define NON_EMPTY 1
#define FREE -1
#define FULL -1
#define ALL -1
#define UNLIMITED -1
#define INTERVALS 60

/* ========================================================================== */

/* Function prototypes made by me */
/* ------------------------------ */

void sort_scheduled(Deque *scheduled_processes);
int comparator(const void *p, const void *q);
void print_statistics(long time, long total, Deque* completed_processes);
void print_running(Process* process, long current_time, long double memory_size, PageFrame* frame);
void print_evicted(PageFrame* evicted, long current_time);
void print_finish(Process* process, long current_time, long remaining);
PageFrame* load_pages(PageFrame *frame, Deque* scheduled_processes, Process* process, char* memory_allocation);
PageFrame* evict_pages(PageFrame* frame, PageFrame* evicted, PageFrame* set_of_pages, long number_to_evict);
int cs_comparator(const void *p, const void *q);

/* ========================================================================== */

/* Main function */
/* ------------- */

int main(int argc, char ** argv){
    
  Deque *incoming_processes, *completed_processes, *scheduled_processes;
  PageFrame* frame;
  char* filename;
  char* scheduling_algorithm;
  char* memory_allocation;
  long memory_size = UNLIMITED, quantum = QUANTUM;
  int i;

  // Initial load of the data – not part of scheduling

  for (i = 1; i < argc; ++i) {
    if (strcmp(argv[i],"-f") == 0) {
      filename = argv[i+1];
    }
    else if (strcmp(argv[i],"-a") == 0){
      scheduling_algorithm = argv[i+1];
    }
    else if (strcmp(argv[i],"-m") == 0){
      memory_allocation = argv[i+1];
    }
    else if (strcmp(argv[i],"-s") == 0){
      memory_size = atol(argv[i+1])/PAGE;
      frame = new_page_frame(memory_size);

    }
    else if (strcmp(argv[i],"-q") == 0){
      quantum = atol(argv[i+1]);
    }
  }

  // Read the file
  FILE *fp; 
  char line[100];
  fp = fopen(filename, "r");
  incoming_processes = new_deque();
  
  while (fgets(line, 100, fp)!=NULL ) {

    long time_arrived, process_id, memory_size_req, job_time;    
    sscanf(line, "%li %li %li %li\n", &time_arrived, &process_id, &memory_size_req, &job_time);
    Process* process = new_process(time_arrived, process_id, memory_size_req/PAGE, job_time);
    deque_insert(incoming_processes, process);

  }

  fclose(fp);

  if (strcmp(scheduling_algorithm, "ff") == 0) {
    quantum = UNLIMITED;
  }

  // Loop until all process are completed
  completed_processes = new_deque();
  scheduled_processes = new_deque();
  long completed = 0, quantum_rem = quantum, time = START, total = incoming_processes -> size;
  Process* running = (Process*)malloc(sizeof(*running));
  
  // run them 
  while (completed < total) {
    
    Deque* received_processes = new_deque();
    int received = 0;

    // receive the processes online style
    while (incoming_processes != NULL && time == incoming_processes->top->process->time_arrived) {
      
      deque_insert(received_processes, deque_pop(incoming_processes));
      received++;
      if (incoming_processes->size == 0){
        free_deque(incoming_processes);
        incoming_processes = NULL;
      }
    }

    // put in queue according to process id
    if (received == 1){
      deque_insert(scheduled_processes, deque_pop(received_processes));
    }
    else if (received > 1) {
        // Find the lowest process_id
        Process* p[received];
        
        for (i=0; i<received; i++){
          p[i] = deque_pop(received_processes);
        }
        if (strcmp(scheduling_algorithm, "cs") == 0) {
          qsort((void*)p, received, sizeof(p[0]), cs_comparator);
        }
        else{
          qsort((void*)p, received, sizeof(p[0]), comparator);
        }
        
        for (i=0; i<received; i++){
          deque_insert(scheduled_processes, p[i]);
        }
    }
    
    // Run the processes
    
    // Run the first process
    if (time == START || (running == NULL && received >= 1)) {
      
      running = deque_pop(scheduled_processes);
      running -> last_executed = time;
      // load memory

      if (strcmp(memory_allocation, "u") != 0) {

        PageFrame* evicted = load_pages(frame, scheduled_processes, running, memory_allocation);
      
        
        if (evicted -> free_space != 0) {
          print_evicted(evicted, time);
        }
        running -> T_rem += PAGE_FAULT * (running -> memory_size_req - running -> set_of_pages -> free_space);
        
      }

      print_running(running, time, memory_size, frame); 
      
      running -> T_rem += running -> T_load;
      if (strcmp(scheduling_algorithm, "ff") != 0) {
        quantum_rem += running -> T_load;
      }
      running -> T_load = 0;
    }

    // Process has completed
    if (running && running -> T_rem == 0) {
      if (strcmp(scheduling_algorithm, "cs") == 0) {
        sort_scheduled(scheduled_processes);
      }

      running -> completion_time = time;
      quantum_rem = quantum;
      deque_insert(completed_processes, running);
      completed++;

      // evict the pages before finishing
    
      if (strcmp(memory_allocation, "u") != 0) {
        PageFrame* evicted = new_page_frame(0);
  
        evict_pages(frame, evicted, running->set_of_pages, ALL); // will always be ALL, cause finished
        remove_all(frame, running -> process_id);
        merge_frame(frame);
       
        frame -> free_space = calculate_free(frame);
        print_evicted(evicted, time);
        
      }
      print_finish(running, time, scheduled_processes -> size);
      
      // Still got more processes to run
      if (completed < total){

        if (scheduled_processes -> size > 0) {

          
          running = deque_pop(scheduled_processes);
          running -> last_executed = time;
          
          if (strcmp(memory_allocation, "u") != 0) {
            PageFrame* evicted = load_pages(frame, scheduled_processes, running, memory_allocation);
          
            if (evicted -> free_space != 0) {
              print_evicted(evicted, time);
            }
            running -> T_rem += PAGE_FAULT * (running -> memory_size_req - running -> set_of_pages -> free_space);
          }
          
          print_running(running, time, memory_size, frame);
          
          running -> T_rem += running -> T_load;
          if (strcmp(scheduling_algorithm, "ff") != 0) {
            quantum_rem += running -> T_load;
          }
          running -> T_load = 0;
        }
        else {
          running = NULL;
        }
      }

      // print statistics
      else if (completed == total) {

        // Print statistics
        print_statistics(time, total, completed_processes);
      }
    }

    // ran out of quantum, time to switch
    else if (strcmp(scheduling_algorithm, "ff") != 0 && quantum_rem == 0 ) {
      
      
      quantum_rem = quantum;
      deque_insert(scheduled_processes, running);

      if (strcmp(scheduling_algorithm, "cs") == 0) {
        
        sort_scheduled(scheduled_processes);
      }
      running = deque_pop(scheduled_processes);
      
      running -> last_executed = time;
      if (strcmp(memory_allocation, "u") != 0) {
        PageFrame* evicted = load_pages(frame, scheduled_processes, running, memory_allocation);
      
        
        if (evicted -> free_space != 0) {
          print_evicted(evicted, time);
        }
        running -> T_rem += PAGE_FAULT * (running -> memory_size_req - running -> set_of_pages -> free_space);
      }
      print_running(running, time, memory_size, frame);
      running -> T_rem += running -> T_load;
      if (strcmp(scheduling_algorithm, "ff") != 0) {
        quantum_rem += running -> T_load;
      }
      running -> T_load = 0;
    
    }
    
    if (running) {
      running -> T_rem--;
    }
    

    if (running && quantum != UNLIMITED && strcmp(scheduling_algorithm, "ff") != 0) {
      quantum_rem--;
    }
    time++;
  }


  free_deque(scheduled_processes);
  free_deque(completed_processes);

  return 0;

}

/* ========================================================================== */

/* Helper functions created by me */
/* ------------------------------ */

void print_statistics(long time, long total, Deque* completed_processes) {
  int interval = ceil(time/INTERVALS), index;
  int throughput[interval];
  long i;
  long double all_time[total];
  long double all_turn_around[total];

  memset(throughput, 0, sizeof(throughput));

  Node* curr = completed_processes -> top;
  i=0;

  index = ceil((long double)curr -> process->completion_time/INTERVALS -1) ;
  throughput[index]++;
  
  long double turn_around = curr -> process->completion_time - curr -> process->time_arrived;
  all_turn_around[i] = turn_around;
  all_time[i] = (turn_around/curr->process->job_time);
  i++;

  while (curr -> prev != NULL) {
    curr = curr -> prev;
    index = ceil((long double)curr -> process->completion_time/INTERVALS -1) ;
    throughput[index]++;
    
    turn_around = curr -> process->completion_time - curr -> process->time_arrived;
    all_turn_around[i] = turn_around;
    all_time[i] = turn_around/curr->process->job_time;
    i++;
  }

  long avg_throughput = 0, min_throughput= __LONG_MAX__, max_throughput=-1;
  for(i = 0; i < interval; i++){
    long n = throughput[i];

    avg_throughput += n;
    if (n <= min_throughput){
      min_throughput = n; 
    }
    if (n >= max_throughput){
      max_throughput = n; 
    }

  }
  
  avg_throughput = ceil((long double)avg_throughput/interval);

  long double avg_turn_around = 0, avg_time = 0, max_time = -1;

  for (i = 0; i < total; i++){
    avg_turn_around += all_turn_around[i];
    long double n = all_time[i];
    avg_time += n;
    if (n > max_time){
      max_time = n; 
    }
  }
  avg_turn_around = ceil(avg_turn_around/total);
  avg_time = avg_time/total;

  printf("Throughput %li, %li, %li\nTurnaround time %.0Lf\nTime overhead %.2Lf %.2Lf\nMakespan %li\n", avg_throughput, min_throughput, max_throughput, avg_turn_around, max_time, avg_time, time);

}

void sort_scheduled(Deque *scheduled_processes) {

 
  long i, scheduled = scheduled_processes->size;
  Process* p[scheduled];
        
  for (i=0; i<scheduled; i++){
    p[i] = deque_pop(scheduled_processes);
  }

  qsort((void*)p, scheduled, sizeof(p[0]), cs_comparator);
  
  for (i=0; i<scheduled; i++){
    deque_push(scheduled_processes, p[i]);
  }
}


PageFrame* evict_pages(PageFrame* frame, PageFrame* evicted, PageFrame* set_of_pages, long number_to_evict) {

  PageFrame* freed_blocks = new_page_frame(-1);

  // remember to edit page frame to say that it's free

  Block* freed = (Block*)malloc(sizeof(*freed));
  freed = set_of_pages -> bottom;
  long current_process = freed -> owner_id;


  if (number_to_evict == ALL) {
    

    while (freed) {
      freed -> owner_id = FREE;

      add_block(evicted, freed);
      
      add_block(freed_blocks, freed);
      
      freed = freed -> next;
    }   
    remove_free(set_of_pages);
  }


  else {
    while (freed && number_to_evict > 0){
      Block* chunk = (Block*)malloc(sizeof(*chunk));
      chunk -> start = freed -> start;
      chunk -> next = NULL;
      chunk -> prev = NULL;
      chunk -> owner_id = FREE;
      // small chunks or just nice
      if (freed -> length <= number_to_evict){
        set_of_pages -> free_space -= freed -> length;
        freed -> owner_id = FREE;
        evicted -> free_space += freed -> length;
        number_to_evict -= freed -> length;

        chunk->length = freed -> length;
           
      }
      
      // big chonker
      else {
 
        set_of_pages -> free_space -= number_to_evict;

        chunk -> length = number_to_evict;

        evicted -> free_space += number_to_evict;
        freed -> length -= number_to_evict;
        freed -> start += number_to_evict;
        
        number_to_evict = 0;
        
      }
      add_block(evicted, chunk);
      add_block(freed_blocks, chunk);
      freed = freed -> next;
    }
  } 
  remove_free(set_of_pages);
 
  // Free the space in the frame
  Block* curr = (Block*)malloc(sizeof(*curr));
  Block* curr_freed = (Block*)malloc(sizeof(*curr_freed));
  curr = frame -> bottom;
  curr_freed = freed_blocks -> bottom;


  while (curr_freed && curr) {
    
    if (curr -> owner_id == current_process && curr_freed -> start == curr -> start) {

      if (curr -> length == curr_freed -> length) {

        curr -> owner_id = FREE;

        frame -> free_space += curr_freed -> length;

      }
      // gotta split the chunk
      else {
        Block* new = (Block*)malloc(sizeof(*new));
        new -> owner_id = FREE;
        new -> start = curr_freed -> start;
        new -> length = curr_freed -> length;
        curr -> length -= curr_freed -> length;
        curr -> start += curr_freed -> length;

        add_block(frame, new);

      }
            
      curr_freed = curr_freed -> next;
    }
    else {
      curr = curr -> next;
    }
    
  }
  merge_frame(frame);

  remove_free(set_of_pages);
  return freed_blocks;
}

PageFrame* load_pages(PageFrame *frame, Deque* scheduled_processes, Process* process, char* memory_allocation) {
  
  long required = process -> memory_size_req - process -> set_of_pages -> free_space;
  PageFrame* evicted = new_page_frame(0);
  Block* free = NULL;
  if (strcmp(memory_allocation, "v") == 0 && frame -> free_space < required) {
    required = PAGE - process -> set_of_pages -> free_space;
    if (required < frame -> free_space) {
      required = frame -> free_space;
    }
  }
  process -> T_load += LOAD_TIME * required;

  while (required != 0) {
    
    free = find_free(frame, required, memory_allocation);
  
    // Give the process all/part of the required free space and keep doing until enough
    if (free) {
      
      Block* given_block = (Block*)malloc(sizeof(*given_block));
      given_block -> prev = given_block -> next = NULL;
      given_block -> owner_id = process -> process_id;
      given_block -> length = free -> length;
      given_block -> start = free -> start;
            
      // if fit nicely or have to fragment the pages we give to the process
      if (free -> length <= required) {

        free -> owner_id = process -> process_id;
        required -= free -> length;

      }
      // if got more than enough, so need to alter the free block
      else {

        Block *new = (Block*)malloc(sizeof(*new));
        assert(new);


        given_block -> length = required;
        new -> length = required;
        new -> prev = NULL;
        
        new -> owner_id = process -> process_id;
        new -> start = free -> start;

        if (free -> start == 0) {
          frame -> bottom = new;
        } 
        else {
          free -> prev -> next = new;
        }  
        free -> start += required;
        free -> length -= required;

        new -> next = free;
        free -> prev = new;

        required = FINISHED;
        

      } 

      add_block(process->set_of_pages, given_block);
      
      frame -> free_space -= given_block -> length;
      
      merge_frame(frame); 
      
    
    }
    // need to evict 
    else {
      Node* curr = scheduled_processes -> bottom;

      Process* to_be_evicted;
      

      // Find the one at the very back of the queue
      if (strcmp(memory_allocation, "cm")==0) {
        Node* next = curr -> next;

        while (next && next -> process -> set_of_pages -> free_space != 0) {
          curr = next;
          next = next -> next;
        }
        to_be_evicted = curr -> process;

      }
      // Find the least recently executed
      else {
        while (curr) {
          long time = __LONG_MAX__;

          if (curr -> process -> last_executed < time && curr -> process -> process_id != process -> process_id && curr -> process -> set_of_pages -> free_space != EMPTY) {
            to_be_evicted = curr -> process;
            time = curr -> process -> last_executed;
          }
          curr = curr -> next;
        }
      }
      long number_to_evict = ALL;
      if (strcmp(memory_allocation, "v") == 0){
        number_to_evict = required;
        if (number_to_evict > to_be_evicted -> set_of_pages -> free_space) {
          number_to_evict = ALL;
        }
      }
      evict_pages(frame, evicted, to_be_evicted->set_of_pages, number_to_evict);

      remove_free(to_be_evicted -> set_of_pages);
      merge_frame(frame);
      frame -> free_space = calculate_free(frame);
    
    }
  }
  
  return evicted;
}



int cs_comparator(const void *p, const void *q) { 
    long l = ((Process *)p)->job_time; 
    long r = ((Process *)q)->job_time;  
    // long l = ((Process *)p)->T_rem; 
    // long r = ((Process *)q)->T_rem;  
    return (l - r); 
} 



int comparator(const void *p, const void *q) { 
    long l = ((Process *)p)->process_id; 
    long r = ((Process *)q)->process_id;  
    return (l - r); 
} 


void print_running(Process* process, long current_time, long double memory_size, PageFrame* frame){

  printf("%li, RUNNING, id=%li, remaining-time=%li", current_time, process->process_id, process->T_rem);

  if (memory_size != UNLIMITED){
    // printf("\nfree space = %li\n", frame-> free_space);
    long mem_usage = ceil(((memory_size - frame -> free_space) / memory_size) * 100);
    printf(", load-time=%li, mem-usage=%li%%, mem-addresses=", process->T_load, mem_usage);
    print_pages(process->set_of_pages);
  }
  printf("\n");
}

void print_evicted(PageFrame* evicted, long current_time){

  printf("%li, EVICTED, mem-addresses=", current_time);
  print_pages(evicted);
  printf("\n");
  
}


void print_finish(Process* process, long current_time, long remaining){
  printf("%li, FINISHED, id=%li, proc-remaining=%li\n", current_time, process->process_id, remaining);
}

