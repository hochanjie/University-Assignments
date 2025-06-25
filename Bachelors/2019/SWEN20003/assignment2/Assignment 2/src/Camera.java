import org.newdawn.slick.Input;

/**
 * Camera class to restrict the game's view to a subset of the entire world.
 *
 * Code taken from the sample solution for the first assignment
 */

public class Camera {

	/* STATIC FINAL CONSTANTS */
	/* ---------------------- */

	private static final int HALF = 2;
	private static final double FREE_SPEED = 0.4;
	private static final double INITIAL_POSITION = 300;


	/* ============================================================================================================== */

	/* INSTANCE VARIABLES */
	/* ------------------ */

	private double x = INITIAL_POSITION;
	private double y = INITIAL_POSITION;
	private double targetX = INITIAL_POSITION;
	private double targetY = INITIAL_POSITION;
	private GameObject target;


	/* ============================================================================================================== */

	/* GETTERS, AND SETTERS */
	/* -------------------- */

	/**
	 * Get the x-coordinates of the top-left corner of the camera
	 *
	 * @return The x-coordinates
	 */

	public double getX() {

		return x;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Get the y-coordinates of the top-left corner of the camera
	 *
	 * @return The y-coordinates
	 */

	public double getY() {

		return y;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Set the GameObject that the camera should follow
	 *
	 * @param target Selected GameObject
	 */

	public void followObject(GameObject target) {

		this.target = target;

	}


	/* ============================================================================================================== */

	/* CALCULATION / CONVERSION METHODS */
	/* -------------------------------- */

	/**
	 * Function to convert a global coordinate to a coordinate relative to the camera's screen
	 *
	 * @param x The global x-coordinate
	 *
	 * @return The x-coordinate relative to the screen
	 */

	public double globalXToScreenX(double x) {

		return x - this.x;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Function to convert a global coordinate to a coordinate relative to the camera's screen
	 *
	 * @param y The global y-coordinate
	 *
	 * @return The y-coordinate relative to the screen
	 */

	public double globalYToScreenY(double y) {

		return y - this.y;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Function to convert a coordinate relative to the camera's screen to the global coordinate
	 *
	 * @param x The x-coordinate relative to the screen
	 *
	 * @return The global x-coordinate
	 */

	public double screenXToGlobalX(double x) {

		return x + this.x;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Function to convert a coordinate relative to the camera's screen to the global coordinate
	 *
	 * @param y The y-coordinate relative to the screen
	 *
	 * @return The global y-coordinate
	 */

	public double screenYToGlobalY(double y) {

		return y + this.y;

	}

	/* ============================================================================================================== */

	/* HELPER METHODS */
	/* -------------- */

	/**
	 * Function to allow the camera alter which subset of the world to show
	 *
	 * @param world World class which would store the last input and which could make the camera move freely
	 */

	public void update(World world) {

		Input input = world.getInput();

		// Allow for free camera movement, which would mean that there's no target to follow

		if (input.isKeyDown(Input.KEY_W)) {

			target = null;
			targetY += world.getDelta() * -FREE_SPEED;

		}

		if (input.isKeyDown(Input.KEY_A)) {

			target = null;
			targetX += world.getDelta() * -FREE_SPEED;

		}
		if (input.isKeyDown(Input.KEY_S)) {

			target = null;
			targetY += world.getDelta() * FREE_SPEED;

		}
		if (input.isKeyDown(Input.KEY_D)) {

			target = null;
			targetX += world.getDelta() * FREE_SPEED;

		}

		// Follow a target if there is one

		if (target != null) {

			targetX = target.getX() - App.WINDOW_WIDTH / HALF;
			targetY = target.getY() - App.WINDOW_HEIGHT / HALF;

		}

		// Make sure camera does not go past map boundary

		x = Math.min(targetX, world.getMapWidth() -	 App.WINDOW_WIDTH);
		x = Math.max(x, 0);
		y = Math.min(targetY, world.getMapHeight() - App.WINDOW_HEIGHT);
		y = Math.max(y, 0);

	}

	/* ============================================================================================================== */

}
