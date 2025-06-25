/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * Process data structure for Assignment 2
 *
 */

#ifndef PROCESS_H
#define PROCESS_H

#include "pageframe.h"

typedef struct process {
  long time_arrived;
  long process_id;
  long memory_size_req; // in pages
  long job_time;
  long T_rem;
  long last_executed;
  long completion_time; 
  long T_load;
  PageFrame* set_of_pages;
} Process;

// Returns a new Process
Process* new_process(long time_arrived, long process_id, long memory_size_req, long job_time);


#endif