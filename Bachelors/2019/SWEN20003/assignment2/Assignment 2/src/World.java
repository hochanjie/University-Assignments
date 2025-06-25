import org.newdawn.slick.Game;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * World class to contain all the different objects in the game world, and schedule their interactions.
 * 
 * Code extended from the sample solution for the first assignment
 */

public class World {

	/* STATIC FINAL CONSTANTS */
	/* ---------------------- */

	private static final String MAP_PATH = "assets/main.tmx";
	private static final String OBJECT_PATH = "assets/objects.csv";
	private static final String SOLID_PROPERTY = "solid";
	private static final String OCCUPIED_PROPERTY = "solid";

	private static final int NONE = 0;
	private static final int FOUND = 1;
	private static final int INITIAL_CAPACITY = 2;
	private static final int MAX_RANGE = 32;
	private static final int TEXT_DISPLAY_X = 32;
	private static final int TEXT_DISPLAY_Y = 100;
	private static final int TYPE_COLUMN = 0;
	private static final int X_COLUMN = 1;
	private static final int Y_COLUMN = 2;


	/* ============================================================================================================== */

	/* INSTANCE VARIABLES */
	/* ------------------ */

	private TiledMap map;
	private Camera camera = new Camera();
	private GameObject selected = null;
	private ArrayList<Units> units = new ArrayList<>();
	private ArrayList<Resources> resources = new ArrayList<>();
	private ArrayList<GameObject> buildings = new ArrayList<>();

	private int totalMetal = NONE;
	private int totalUnobtainium = NONE;
	private int carryingCapacity = INITIAL_CAPACITY;
	private int lastDelta;
	private Input lastInput;

	/* ============================================================================================================== */

	/* CONSTRUCTORS, GETTERS, AND SETTERS */
	/* ---------------------------------- */


	/**
	 * World constructor that loads the initial GameObjects and sets the map
	 *
	 * @throws SlickException
	 */

	public World() throws SlickException {

		readCSV();
		map = new TiledMap(MAP_PATH);

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the total width of the entire map
	 *
	 * @return Width of entire map
	 */

	public double getMapWidth() {

		return map.getWidth() * map.getTileWidth();

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the total height of the entire map
	 *
	 * @return Height of entire map
	 */

	public double getMapHeight() {

		return map.getHeight() * map.getTileHeight();

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the camera used to focus on a subset of the map of the world
	 *
	 * @return Camera
	 */

	public Camera getCamera() {

		return camera;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the currently selected GameObject
	 *
	 * @return Currently selected GameObject
	 */

	public GameObject getSelected() {

		return selected;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the list that holds all the Units in the world
	 *
	 * @return Array list of Units
	 */

	public ArrayList<Units> getUnits() {

		return units;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the list that holds all the Resources in the world
	 *
	 * @return Array list of Resources
	 */

	public ArrayList<Resources> getResources() {

		return resources;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the list that holds all the Buildings (of type GameObject) in the world
	 *
	 * @return Array list of Buildings
	 */

	public ArrayList<GameObject> getBuildings() {

		return buildings;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the total amount of metal that has been mined and dropped off at a command centre
	 *
	 * @return Total amount of metal collected
	 */

	public int getTotalMetal() {

		return totalMetal;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Increase / Decrease the total amount of metal collected
	 *
	 * @param amount Amount to change the total by
	 */

	public void changeTotalMetal(int amount) {

		this.totalMetal += amount;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Increase the total amount of unobtainium collected
	 *
	 * @param amount Amount to increase the total by
	 */

	public void increaseTotalUnobtainium(int amount) {

		this.totalUnobtainium += amount;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the max number of resources the Engineers can carry at once
	 *
	 * @return Carrying capacity of all the Engineers
	 */

	public int getCarryingCapacity() {

		return carryingCapacity;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Increment the carrying capacity of the Engineers by 1
	 */

	public void increaseCarryingCapacity() {

		this.carryingCapacity++;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the amount of time (in milliseconds) that has passed since the last frame
	 *
	 * @return Amount of time passed in milliseconds
	 */

	public int getDelta() {

		return lastDelta;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Gets the last input given by the user
	 *
	 * @return Last input given by user
	 */

	public Input getInput() {

		return lastInput;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Checks if the position / tile is free so that a unit can move on it
	 *
	 * @param x The x-coordinate to check which tile to check
	 * @param y The y-coordinate to check which tile to check
	 *
	 * @return Property of the tile
	 */

	public boolean isPositionFree(double x, double y) {

		int tileId = map.getTileId(worldXToTileX(x), worldYToTileY(y), 0);
		return !Boolean.parseBoolean(map.getTileProperty(tileId, SOLID_PROPERTY, "false"));

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Checks if the position / tile is occupied which means a new building cannot be built on it
	 *
	 * @param x The x-coordinate to check which tile to check
	 * @param y The y-coordinate to check which tile to check
	 *
	 * @return Property of the tile
	 */

	// In my implementation, this only checks the tile property. It does not check if there's already a building at that
	// spot. In other words, you can have fun building buildings on top of each other

	public boolean isPositionOccupied(double x, double y) {

		int tileId = map.getTileId(worldXToTileX(x), worldYToTileY(y), 0);
		return !Boolean.parseBoolean(map.getTileProperty(tileId, OCCUPIED_PROPERTY, "false"));

	}


	/* ============================================================================================================== */

	/* CALCULATION / CONVERSION METHODS */
	/* -------------------------------- */

	/**
	 * Calculates the distance between two points
	 *
	 * @param x1 The x-coordinates of the first point
	 * @param y1 The y-coordinates of the first point
	 * @param x2 The x-coordinates of the second point
	 * @param y2 The y-coordinates of the second point
	 *
	 * @return The distance between the points
	 */

	public static double distance(double x1, double y1, double x2, double y2) {

		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	private int worldXToTileX(double x) {

		return (int) (x / map.getTileWidth());

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	private int worldYToTileY(double y) {

		return (int) (y / map.getTileHeight());

	}

	/* ============================================================================================================== */

	/* HELPER METHODS */
	/* -------------- */

	/* Read the CSV and add all the GameObjects and Resources within into the world */

	private void readCSV() {

		try (BufferedReader br = new BufferedReader(new FileReader(OBJECT_PATH))) {

			String text;

			// Read in the line and split it by the comma

			while ((text = br.readLine()) != null) {

				String columns[] = text.split(",");
				String type = columns[TYPE_COLUMN];
				Double x = Double.parseDouble(columns[X_COLUMN]);
				Double y = Double.parseDouble(columns[Y_COLUMN]);

				// Use switch case to find the type of the object to be added, and set the engineer as the first
				// selected

				switch (type) {

					case "command_centre":

						buildings.add(new CommCentre(x, y));
						break;

					case "pylon":

						buildings.add(new Pylon(x, y));
						break;

					case "engineer":

						Units engineer = new Engineer(x, y);
						units.add(engineer);
						selected = engineer;
						camera.followObject(engineer);
						break;

					default:

						resources.add(new Resources(type, x, y));

				}

			}
		}

		catch (Exception e) {

			e.printStackTrace();

		}

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/* Selects the closest GameObject to the click */

	private void selectClosest(double x, double y) {

		int isInRange = NONE;

		for (GameObject object: buildings) {

			// Find if there are any buildings that are within the 32 pixel range of the click

			isInRange += isInRange(object, x, y);

		}

		for (GameObject object: units) {

			// Find if there are any units that are within the 32 pixel range of the click

			isInRange += isInRange(object, x, y);

		}


		// If there were no objects within the range

		if (isInRange == NONE) {

			selected = null;

		}

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/* Checks if the object is in range */

	private int isInRange(GameObject object, double x, double y) {

		double objectToMouse = distance(camera.globalXToScreenX(object.getX()),
				camera.globalYToScreenY(object.getY()), x, y);


		if (objectToMouse < MAX_RANGE) {

			// The first object within the range is set as the first selected object

			if (selected == null) {

				selected = object;

			}

			// Second object in range onwards must be checked with current gameObject in selected

			else {

				double selectedToMouse = distance(camera.globalXToScreenX(selected.getX()),
						camera.globalYToScreenY(selected.getY()), x, y);

				// Compares distance between click and the object vs the distance between click and the current selected

				if (objectToMouse < selectedToMouse) {

					selected = object;

				}
				else {

					// If the distance is the same, choose the unit

					if (objectToMouse == selectedToMouse) {

						if ((object instanceof Units) || !(selected instanceof Units)) {

							selected = object;

						}

					}

				}

			}

			return FOUND;

		}

		return NONE;

	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Update functions which updates the camera, selects a GameObject, create GameObjects, and then updates the
	 * GameObjects within the world
	 *
	 * @param input
	 * @param delta
	 *
	 * @throws SlickException
	 */

	public void update(Input input, int delta) throws SlickException {

		lastInput = input;
		lastDelta = delta;

		camera.update(this);

		// Check if the user wants to select an object

		if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {

			selectClosest(input.getMouseX(), input.getMouseY());
			camera.followObject(selected);

		}

		// Check if the user wants to move the selected

		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {

			if (selected != null) {

				selected.setTarget(camera.screenXToGlobalX(input.getMouseX()),
						camera.screenYToGlobalY(input.getMouseY()));

			}

		}

		// Update all the other objects to carry out their tasks

		for (GameObject object : buildings) {

			object.update(this);

		}


		GameObject removeTruck = null;

		for (GameObject object : units) {

			object.update(this);

			// If it's a truck, check if it has finished creating its command centre and if yes, deselect if necessary,
			// assign it to a temporary variable, and remove it outside this loop

			if (object instanceof Truck && ((Truck) object).isDone()) {

				if (selected == object) {

					selected = null;

				}

				removeTruck = object;

			}

		}

		if (removeTruck != null) {

			units.remove(removeTruck);

		}


	}


	/* -------------------------------------------------------------------------------------------------------------- */

	/**
	 * Render function to draw the map, then the highlight under the selected GameObject, all the GameObjects and
	 * Resources over it, and lastly the text displays that show the amount of Resources collected and instructions
	 *
	 * @param g The Slick graphics object, used for drawing.
	 */

	public void render(Graphics g) {

		// Render the map first then the highlight of the selected object

		map.render((int) camera.globalXToScreenX(0), (int) camera.globalYToScreenY(0));

		if (selected != null) {

			selected.highlight(camera);

		}

		// Should have building be rendered first since they are the largest, then the resources, then the units

		for (GameObject object : buildings) {

			object.render(camera);

		}

		for (Resources resources : resources) {

			resources.render(camera);

		}

		for (GameObject object : units) {

			object.render(camera);

		}

		// Draw the text display last so that it is above everything else

		String collected = String.format("Metal: %d\nUnobtainium: %d", totalMetal, totalUnobtainium);
		g.drawString(collected, TEXT_DISPLAY_X, TEXT_DISPLAY_X);

		if (selected != null) {

			g.drawString(selected.toString(), TEXT_DISPLAY_X, TEXT_DISPLAY_Y);

		}

	}

	/* ============================================================================================================== */

}