package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;

import java.util.List;
import java.util.Map;

/**
 * An object that can find a path from one coordinate to another within a map
 */
public interface Pathfinder {
    List<Coordinate> getPath(Coordinate from, Coordinate to, Map<Coordinate,MapTile> map);
}
