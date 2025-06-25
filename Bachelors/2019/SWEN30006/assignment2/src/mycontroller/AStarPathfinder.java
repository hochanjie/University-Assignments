package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * AStarPathfinder class that uses the A* pathfinder algorithm to determine what is the best route to take to the
 * nearest parcel/exit
 */
public class AStarPathfinder implements Pathfinder {
    private Predicate<MapTile> canVisit;

    /**
     * AStarPathFinder constructor that sets the tiles that can be visited
     *
     * @param canVisit Tiles that can be visited (i.e. not walls)
     */
    public AStarPathfinder(Predicate<MapTile> canVisit) {
        this.canVisit = canVisit;
    }

    /**
     * Function that creates the best route to take from one coordinate to another within the map, avoiding the walls
     *
     * @param from The coordinate of the starting tile
     * @param to The coordinate of the destination tile
     * @param map The map of the world
     * @return The best route from the starting tile to the destination tile
     */
    @Override
    public List<Coordinate> getPath(Coordinate from, Coordinate to, Map<Coordinate,MapTile> map) {
        // Initialize the priority queue, result path and node map
        var path = new LinkedList<Coordinate>();
        var nodeMap = map.keySet().stream()
            .collect(Collectors.toMap(c -> c, c -> new Node(c, map.get(c), Double.POSITIVE_INFINITY)));
        Node start = nodeMap.get(from);
        start.cost = start.distanceTo(to);
        var nodes = new PriorityQueue<>(nodeMap.values());

        while (!nodes.isEmpty()) {
            // Visit the next best node from the priority queue
            Node current = nodes.remove();

            // Stop if we've found our destination
            if (current.coord.equals(to)) {
                Node parent = current;
                do {
                    path.addFirst(parent.coord);
                } while ((parent = parent.parent) != null);
                break;
            }

            // Explore the adjacent nodes, checking if they achieve smaller costs
            for (Node next : current.getAdjacent(nodeMap)) {
                double newCost = current.cost + next.distanceTo(to) - current.distanceTo(to) + 1;
                if (newCost < next.cost) {
                    nodes.remove(next);
                    next.cost = newCost;
                    next.parent = current;
                    nodes.add(next);
                }
            }
        }
        return path;
    }

    /**
     * Inner class representing a node wrapping each coordinate, with the best current cost and the parent node.
     */
    private class Node implements Comparable<Node> {
        private final Coordinate coord;
        private final MapTile tile;
        private double cost;
        private Node parent;

        /**
         * Constructor that sets the coordinates and cost of the Node
         *
         * @param coord The coordinates of the node (will not change)
         * @param cost The starting cost to reach this node
         */
        private Node(Coordinate coord, MapTile tile, double cost) {
            this.coord = coord;
            this.tile = tile;
            this.cost = cost;
        }

        /**
         * Function to get a list of the Nodes that are adjacent to the coordinates of a Node instance
         *
         * @param nodeMap Map of the Nodes created in the world
         * @return List of the adjacent Nodes
         */
        public List<Node> getAdjacent(Map<Coordinate, Node> nodeMap) {
            var neighbors = new ArrayList<Node>();
            for (Coordinate dir : WorldUtil.CARDINAL_DIRECTIONS) {
                var c = new Coordinate(coord.x + dir.x, coord.y + dir.y);
                if (nodeMap.containsKey(c) && canVisit.test(nodeMap.get(c).tile)) {
                    neighbors.add(nodeMap.get(c));
                }
            }
            return neighbors;
        }

        /**
         * Function to calculate the distance from one coordinate to the coordinate of this instance
         *
         * @param pos Coordinate to calculate the distance from
         * @return The distance between the two coordinates
         */
        public double distanceTo(Coordinate pos) {
            return Math.hypot(coord.x - pos.x, coord.y - pos.y);
        }

        /**
         * Function to compare the cost of this instance against the cost of another instance
         *
         * @param other Another instance of a Node to compare to
         * @return Positive if the cost of this instance is higher, 0 if equal, negative if lower
         */
        @Override
        public int compareTo(Node other) {
            return Double.compare(cost, other.cost);
        }
    }
}
