package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;

import java.util.*;
import java.util.function.Predicate;

/**
 * BreadFirstSearcher class that uses a breadth first search algorithm to find the nearest tile depending on the
 * condition/property given
 */
public class BreadthFirstSearcher implements Searcher {
    private Predicate<MapTile> canVisit;

    /**
     * BreadthFirstSearch constructor that sets the MapTile of the tiles that can be visited by the car (i.e. not walls)
     *
     * @param canVisit Tiles that can be visited
     */
    public BreadthFirstSearcher(Predicate<MapTile> canVisit) {
        this.canVisit = canVisit;
    }

    /**
     * Function to find the nearest tile that matches the condition given using a breadth first search algorithm
     *
     * @param start The coordinates of the starting tile
     * @param memory A map of the tiles in the world that we have seen
     * @param condition The type of tile that we want to be finding for (e.g. Parcel, Exit, etc.)
     * @return The coordinates of the nearest tile that matches the condition given
     */
    @Override
    public Coordinate findNearest(Coordinate start, Map<Coordinate,MapTile> memory, Predicate<Coordinate> condition) {
        // Perform a breadth-first search to find the first tile matching the condition
        Queue<Coordinate> cells = new LinkedList<>();
        Set<Coordinate> explored = new HashSet<>();
        cells.add(start);
        explored.add(start);
        while (!cells.isEmpty()) {
            Coordinate current = cells.remove();
            if (condition.test(current)) {
                return current;
            }
            for (Coordinate next : WorldUtil.getAdjacent(current, memory)) {
                if (canVisit.test(memory.get(next)) && !explored.contains(next)) {
                    cells.add(next);
                    explored.add(next);
                }
            }
        }
        return null;
    }
}
