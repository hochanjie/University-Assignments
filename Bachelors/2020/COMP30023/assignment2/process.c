/* Assignment 2 COMP30023: Computer Systems Semester 1 2020
 * Student Name: Chan Jie Ho
 * Student Number: 961948
 * 
 * Process data structure for Assignment 2
 */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "process.h"
#include "pageframe.h"

#define EMPTY 0
#define UNLIMITED -1

// Returns a new Point
Process* new_process(long time_arrived, long process_id, long memory_size_req, long job_time) {

  Process *process = (Process*)malloc(sizeof(*process));
  assert (process);
  process -> time_arrived = time_arrived;
  process -> process_id = process_id;
  process -> memory_size_req = memory_size_req; // in pages
  process -> job_time = job_time;
  process -> T_rem = job_time;
  process -> last_executed = __LONG_MAX__;
  process -> completion_time = UNLIMITED;
  process -> T_load = EMPTY;
  process -> set_of_pages = new_page_frame(UNLIMITED);

  return process;
}
