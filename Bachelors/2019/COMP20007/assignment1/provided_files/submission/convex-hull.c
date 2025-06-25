/* * * * * * *
 * Implementation of the Inside Hull algorithm for Assignment 1
 *
 * created for COMP20007 Design of Algorithms 2019
 * template by Tobias Edwards <tobias.edwards@unimelb.edu.au>
 * implementation by Chan Jie Ho
 */

//                   WRITE YOUR IMPLEMENTATION HERE
//
// You should fill in the function definitions for orientation() and
// inside_hull() in this file.
//
// Don't be shy to add any extra functions or types you may need.

#include <stdio.h>
#include <stdlib.h>

#include "convex-hull.h"
#include "deque.h"

// Returns the orientation of Point p2 in relation to the line segment p0p1.
// If p2 is to the left of p0p1 then it returns LEFT ('l'), if p2 is to the
// right it returns RIGHT ('r').
// If p0, p1 and p2 are collinear then COLLINEAR ('c') is returned.
char orientation(Point p0, Point p1, Point p2) {
  // TODO: Implement orientation()
  
  double position;

  // Cross product between the vectors (vector from p0 to p1 and p0 to p2)
  // where if its cross product (position) is negative, it is to the left
  // of the line, but is to the right of the line if it is positive, and is 
  // on the line if it is equal to 0.
  position = (p2.x - p0.x) * (p1.y - p0.y) - (p2.y - p0.y) * (p1.x - p0.x);
  if (position < ON_THE_LINE) {
    return LEFT;
  }
  else if (position > ON_THE_LINE) {
    return RIGHT;
  }
  else {
    return COLLINEAR;
  }

}

// Takes a polygon (i.e. an array of points) given in counter-clockwise order
// with n points.
//
// Stores the points of the convex hull into the hull array (the last point
// should NOT be the same as the first point), and returns the number of
// points which are in the convex hull.
//
// If three successive points in the polygon are collinear then the algorithm
// should terminate and COLLINEAR_POINTS should be returned.
//
// If an error occurs this function should return INSIDE_HULL_ERROR.
int inside_hull(Point *polygon, int n, Point *hull) {
  // TODO: Implement the InsideHull algorithm

  int i, size;
  Point pi;
  Deque* deque = new_deque();

  //  confirm the input is valid 
  
  for (i = FIRST ; i < n ; i++) {
    
    // Check if the points are collinear, including points across boundary

    if (orientation(polygon[i], polygon[(i + SECOND) % n], 
    polygon[(i + THIRD) % n]) == COLLINEAR) {
      return COLLINEAR_POINTS;
    }
  }
  
  
  //  if Left(p0, p1, p2) then
  //    C ← new deque ⟨p2, p0, p1, p2⟩ 
  //  else
  //    C ← new deque ⟨p2, p1, p0, p2⟩

  deque_insert(deque, polygon[THIRD]);

  if (orientation(polygon[FIRST], polygon[SECOND],polygon[THIRD]) == LEFT) {
    deque_push(deque, polygon[FIRST]);
    deque_push(deque, polygon[SECOND]);
  }
  
  else {
    deque_push(deque, polygon[SECOND]);
    deque_push(deque, polygon[FIRST]);
  }

  deque_push(deque, polygon[THIRD]);

  // Start from next point

  i = FOURTH;

  while (i < n) {

    //  Get next point pi

    pi = polygon[i];

    // Check if the point is outside the boundary created by the deque (both left == neither right)

    if (!(Right(deque, TOP, pi) || Right(deque, BOTTOM, pi))) {
      i++;
      continue;
    }

    // if true then keep popping/removing until you reach a point that would give you an edge with the unknown point
    // that, when pushed/inserted into the partial convex hull, would include all popped/removed points

    while (Right(deque, TOP, pi)){
      deque_pop(deque);
    }

    deque_push(deque, pi);

    while (Right(deque, BOTTOM, pi)){
      deque_remove(deque);
    }

    deque_insert(deque, pi);
    
    i++;
  }
  
  size = deque_size(deque);

  // Move the points in deque to the hull array except for the last node

  for (i = FIRST ; i < size - LAST ; i++) {
    hull[i] = deque_remove(deque);
  }

  // Free the deque

  free_deque(deque);
  deque = NULL;

  return i;
}

// Check if the new point is to the right of the edge

int Right(Deque *deque, int end, Point pi) {

  if (end == TOP) {
    return orientation(deque -> top -> prev -> data, deque -> top -> data, pi) == RIGHT;
  }

  else {
    return orientation(deque -> bottom -> data, deque -> bottom -> next -> data, pi) == RIGHT;
  }
}