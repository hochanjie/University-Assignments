package mycontroller;

import controller.CarController;
import world.Car;

import java.util.*;

import tiles.MapTile;
import utilities.Coordinate;
import world.World;
import world.WorldSpatial;

/**
 * A class for directly controlling a car, changing its steering/acceleration in order to move it according to a path
 * as supplied by its companion class MyAutoPlanner.
 */
public class MyAutoController extends CarController {
	private static final int CAR_SPEED = 1;

	private MyAutoPlanner planner = new MyAutoPlanner(this);
	private Iterator<Coordinate> path;
	private Coordinate dest;
	private int velocity = 0;

	/**
	 * MyAutoController constructor that sets the car that will be controlled
	 *
	 * @param car Car that MyAutoController will control
	 */
	public MyAutoController(Car car) {
		super(car);
	}

	/**
	 * Function that makes the moves the car according to the path that it is supplied with
	 */
	@Override
	public void update() {
		planner.update();
		if (path == null || !path.hasNext()) {
			path = planner.getPath().iterator();
		}
		Coordinate current = new Coordinate(getPosition());

		// Follow the current path, navigating towards the next
		while (dest == null || dest.equals(current)) {
			// Error check for an empty path
			if (!path.hasNext()) {
				if (getView().get(current).isType(MapTile.Type.FINISH) && hasEnoughParcels()) {
					return; // success, test finished
				}
				throw new IllegalStateException("Planner did not provide a path to follow");
			}

			dest = path.next();
		}
		if (Math.abs(dest.x - current.x) >= CAR_SPEED && Math.abs(dest.y - current.y) >= CAR_SPEED) {
			throw new IllegalStateException("Cannot navigate from " + current + " to " + dest + " (too far away)");
		}
		if (dest.y < current.y) {
			move(WorldSpatial.Direction.SOUTH); // note: a lower y coordinate means south
		} else if (dest.y > current.y) {
			move(WorldSpatial.Direction.NORTH);
		} else if (dest.x > current.x) {
			move(WorldSpatial.Direction.EAST);
		} else if (dest.x < current.x) {
			move(WorldSpatial.Direction.WEST);
		}
	}

	/**
	 * Function that decides if the car should accelerate forward or backward, or if it needs to turn left or right
	 *
	 * @param target Direction that the car needs to go
	 */
	private void move(WorldSpatial.Direction target) {
		var direction = getOrientation();
		var left = WorldSpatial.changeDirection(direction, WorldSpatial.RelativeDirection.LEFT);

		if (direction == target && velocity > 0 || direction == WorldSpatial.reverseDirection(target) && velocity < 0) {
			// Already moving towards the target; don't need to do anything
		} else if (direction == target) {
			// Looking the right way but not moving forward
			applyForwardAcceleration();
			velocity += CAR_SPEED;
		} else if (direction == WorldSpatial.reverseDirection(target)) {
			// Looking the opposite way and moving forward
			applyReverseAcceleration();
			velocity -= CAR_SPEED;
		} else {
			// Need to turn
			if (target == left) {
				turnLeft();
			} else {
				turnRight();
			}
		}
	}

	/**
	 * Function that checks if a tile can be visited by the car
	 *
	 * @param tile Tile to be checked
	 * @return True if it can be visited
	 */
	public static boolean canVisit(MapTile tile) {
		return !tile.isType(MapTile.Type.WALL) && !tile.isType(MapTile.Type.EMPTY);
	}

	/**
	 * Function to check if we have already collected enough parcels
	 *
	 * @return True if the number of parcels found are enough to satisfy the requirement
	 */
	public boolean hasEnoughParcels() {
		return numParcelsFound() >= numParcels();
	}

	/**
	 * Get the current velocity, as tracked by the controller.
	 *
	 * @return The car's current velocity.
	 */
	public int getVelocity() {
		return velocity;
	}
}
