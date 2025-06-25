package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;

import java.util.Map;
import java.util.function.Predicate;

/**
 * An object that can search a map for the nearest tile matching a specified condition
 */
public interface Searcher {
    Coordinate findNearest(Coordinate start, Map<Coordinate,MapTile> memory, Predicate<Coordinate> condition);
}
