import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

/**
 * This class should be used to contain all the different objects in your game world, and schedule their interactions.
 * 
 * You are free to make ANY modifications you see fit.
 * These classes are provided simply as a starting point. You are not strictly required to use them.
 */

public class World {
	
	// CONSTANTS AND INSTANCE VARIABLES //
	// -------------------------------- //
	
	public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final int TILE = 64;
    public static final int BASE_LAYER = 0;
    public static final int INITIAL = -300;
    public static final int SQUARE = 2;
    public static final int MIDDLE = 2;
    public static final double STOP = 0.25;
    public static final double SPEED = 0.25;
    
    private static double angle = 0;
	private static double distance = 0;
	private static double distanceX = 0;
	private static double distanceY = 0;
    
	private static TiledMap tiledMap;
	private static Character player;
	private static Camera camera;
	
	/* ========================================================================== */
	
	// WORLD CONSTRUCTOR //
	// ----------------- //
	
	public World() throws SlickException {
		
		// Create all the objects needed in game 
		
		tiledMap = new TiledMap("oosd-project1-package/assets/main.tmx");
		player = new Character("oosd-project1-package/assets/scout.png", 
				WINDOW_WIDTH/MIDDLE, WINDOW_HEIGHT/MIDDLE);
		camera = new Camera(INITIAL, INITIAL);

	}
	
	/* -------------------------------------------------------------------------- */
	
	// TILE PROPOERTY CHECKER //
	// ---------------------- //
	
	public static boolean isSolid(TiledMap tiledMap, Character player, 
			Camera camera, double velocityX, double velocityY) {
		
		// Get the tile coordinates 
		
		int tileX = (int) player.getWorldX(camera, velocityX)/TILE;
		int tileY = (int) player.getWorldY(camera, velocityY)/TILE;
		
		// Get the corresponding tile ID
		
		int tileID = tiledMap.getTileId(tileX, tileY, BASE_LAYER);
		
		// Check the property
		
		boolean solid = Boolean.parseBoolean(tiledMap.getTileProperty(tileID, 
				"solid", "true"));
		
		return solid;
	
	}
	
	/* -------------------------------------------------------------------------- */
	
	// MOVER FUNCTION //
	// -------------- //
	
	public static void move(Character player, Camera camera, 
			double velocityX, double velocityY) {
		
		
		// Moves the player horizontally while keeping it centred unless camera is at
		// the edge 
		
		if (camera.outsideX(velocityX) || player.notCentredX(velocityX)) {
			
			player.moveX(velocityX);
			
		}
		
		else {
			
			camera.moveX(-velocityX);
						
		}
		
		// Moves the player vertically while keeping it centred unless camera is at
		// the edge 
		
		if (camera.outsideY(velocityY) || player.notCentredY(velocityY)) {
			
			player.moveY(velocityY);
			
		}
		
		else {		
				
			camera.moveY(-velocityY);
			
		}

	}
	
	/* -------------------------------------------------------------------------- */
	
	// WORLD UPDATE //
	// ------------ //
	
	public void update(Input input, int delta) {
		
		// Check if the mouse was right-clicked
		
		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
			
			// Get the mouse coordinates 
			
			double mouseX = input.getMouseX();
			double mouseY = input.getMouseY();
			
			// Calculate the distance between the mouse click and the player
			
			distanceX = mouseX - player.getX(); 
			distanceY = mouseY - player.getY();
			angle = Math.atan2(distanceY, distanceX);
			distance = Math.sqrt(Math.pow(distanceX, SQUARE) + 
					Math.pow(distanceY, SQUARE));
			
		}
		
		
		if (distance > STOP) {
			
			// Calculate how much the player should move 
			
			double travelled = SPEED * delta;
			double velocityX = travelled * Math.cos(angle);
			double velocityY = travelled * Math.sin(angle);
			
			// Check if next tile is solid
			
			if (!isSolid(tiledMap, player, camera, velocityX, velocityY)) {
								
				move(player, camera, velocityX, velocityY);
				distance -= travelled;
				
			}
			
			else {
				
				distance = STOP;
				
			}
		}
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// WORLD RENDER //
	// ------------ //
	
	public void render(Graphics g) throws SlickException {
		
		// Render map using camera's coordinates and also player
		
		tiledMap.render((int)camera.getX(), (int)camera.getY());
		player.render();
		
	}
	
	/* ========================================================================== */
	
}

