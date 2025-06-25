package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.WorldSpatial;

import java.util.*;

/**
 * WorldUtil class holds the utility methods that deal with the map of the world
 */
public class WorldUtil {
    public static final Coordinate[] CARDINAL_DIRECTIONS = Arrays.stream(WorldSpatial.Direction.values())
        .map(WorldUtil::directionDelta).toArray(Coordinate[]::new);
    public static final int MAX_ADJACENT = 4;

    private WorldUtil() {} // prevents instantiation

    /**
     * Get all the coordinates of tiles adjacent to a given coordinate on a map.
     *
     * @param coord The coord to check from.
     * @param map The map to look through.
     * @return A list of the adjacent coordinates. The length will not be greater than MAX_ADJACENT.
     */
    public static List<Coordinate> getAdjacent(Coordinate coord, Map<Coordinate, MapTile> map) {
        List<Coordinate> adjacent = new ArrayList<>();
        for (var dir : CARDINAL_DIRECTIONS) {
            var next = new Coordinate(coord.x + dir.x, coord.y + dir.y);
            if (map.containsKey(next)) {
                adjacent.add(next);
            }
        }
        return adjacent;
    }

    /**
     * Calculate the unit vector representing a given cardinal direction
     *
     * @param direction The direction to calculate.
     * @return A unit vector representing that direction, given as a Coordinate.
     */
    public static Coordinate directionDelta(WorldSpatial.Direction direction) {
        switch (direction) {
            case NORTH: return new Coordinate(0, 1);
            case SOUTH: return new Coordinate(0, -1);
            case EAST: return new Coordinate(1, 0);
            case WEST: return new Coordinate(-1, 0);
        }
        throw new IllegalArgumentException("Unknown direction: " + direction);
    }
}
