package mycontroller;

import tiles.MapTile;
import tiles.ParcelTrap;
import utilities.Coordinate;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static mycontroller.WorldUtil.*;

/**
 * MyAutoPlanner class that plans the route the car takes
 */
public class MyAutoPlanner {
    private static final Pathfinder PATHFINDER = new AStarPathfinder(MyAutoController::canVisit);
    private static final Searcher SEARCHER = new BreadthFirstSearcher(MyAutoController::canVisit);

    private MyAutoController controller;
    private Map<Coordinate, MapTile> memory = new HashMap<>();

    /** Ordered priorities for determining a tile to go to. Earlier ones will be chosen before later ones. */
    private final List<TileSelector> priorities = List.of(
        // 1. If we have enough parcels, choose the exit
        new TileSelector(() -> controller.hasEnoughParcels(), coord -> memory.get(coord).isType(MapTile.Type.FINISH)),
        // 2. Choose a parcel
        new TileSelector(coord -> memory.get(coord) instanceof ParcelTrap),
        // 3. Choose a tile on the edge of the planner's vision (in order to explore
        new TileSelector(coord -> getAdjacent(coord, memory).size() < MAX_ADJACENT),
        // 4. As a last resort, choose any other free tile
        new TileSelector(coord -> !coord.equals(new Coordinate(controller.getPosition())))
    );

    /**
     * MyAutoPlanner constructor that sets the controller that MyAutoPlanner will plan routes for
     *
     * @param controller The controller that will be using this instance of MyAutoPlanner
     */
    public MyAutoPlanner(MyAutoController controller) {
        this.controller = controller;
    }

    /**
     * Function that gets the path created by the pathfinder
     *
     * @return The path given by the pathfinder
     */
    public List<Coordinate> getPath() {
        var p = PATHFINDER.getPath(getStartingTile(), chooseDestination(), memory);
        return p;
    }

    /**
     * Function to update the map that the MyAutoPlanner stores as more of the map comes into view
     */
    public void update() {
        // Get new input from the car and update knowledge of the world
        var currentView = controller.getView();
        memory.putAll(currentView);
    }

    /**
     * Function that uses the searcher to find the nearest tile matching the type that is being prioritised
     *
     * @return The nearest tile matching the type being prioritised that the pathfinder should plan to go to
     */
    private Coordinate chooseDestination() {
        Coordinate destination = null;
        Coordinate current = new Coordinate(controller.getPosition());
        for (var tileSelector : priorities) {
            if (tileSelector.applicable.getAsBoolean()) {
                destination = SEARCHER.findNearest(current, memory, tileSelector.condition);
                if (destination != null) {
                    break;
                }
            }
        }
        if (destination == null) {
            throw new IllegalStateException("Planner's search for a destination tile found no possibilities");
        }
        return destination;
    }

    /**
     * Inner class to represent the TileSelector that is responsible for selecting the Tile that matches the properties
     * or conditions that we are prioritising
     */
    private static class TileSelector {
        public final BooleanSupplier applicable;
        public final Predicate<Coordinate> condition;

        /**
         * TileSelector constructor that sets the condition the tiles are to be checked against as well as whether the
         * condition can/should be applied
         *
         * @param applicable Determines if the condition should be applicable at a point in time
         * @param condition Type (E.g. Wall, Parcel, Exit) that is being prioritised
         */
        public TileSelector(BooleanSupplier applicable, Predicate<Coordinate> condition) {
            this.applicable = applicable;
            this.condition = condition;
        }

        /**
         * Overloaded TileSelector constructor that sets the condition the tiles are to be checked against and
         * automatically sets it applicability to true
         *
         * @param condition Type (E.g. Wall, Parcel, Exit) that is being prioritised
         */
        public TileSelector(Predicate<Coordinate> condition) {
            this.applicable = () -> true;
            this.condition = condition;
        }
    }

    /**
     * Get a tile to start finding a path from.
     *
     * @return either the current tile (if moving), the tile in front, or if that is unavailable, the tile behind
     */
    private Coordinate getStartingTile() {
        var current = new Coordinate(controller.getPosition());
        if (controller.getVelocity() != 0) {
            return current;
        }

        // If the car is not moving, it cannot turn and so we need to ignore the tiles on either side as possible
        // initial moves; instead, pick either the tile in front or the tile behind the car.
        var dir = directionDelta(controller.getOrientation());
        var front = new Coordinate(current.x + dir.x, current.y + dir.y);
        if (memory.containsKey(front) && MyAutoController.canVisit(memory.get(front))) {
            return front;
        }
        return new Coordinate(current.x - dir.x, current.y - dir.y);
    }
}
