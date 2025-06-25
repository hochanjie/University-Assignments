/**
 * This class should be used to restrict the game's view to a subset of the entire world.
 * 
 * You are free to make ANY modifications you see fit.
 * These classes are provided simply as a starting point. You are not strictly required to use them.
 */


public class Camera extends GameObject implements Movable {
	
	// CONSTANTS AND INSTANCE VARIABLES //
	// -------------------------------- //
	
	public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final int LEFT_EDGE = -1920;
    public static final int RIGHT_EDGE = 0;
    public static final int TOP_EDGE = -1920;
    public static final int BOTTOM_EDGE = 0;
    	
	/* ========================================================================== */
	
	// CAMERA CONSTRUCTOR //
	// ------------------ //
	
	public Camera(double x, double y){
		
		super(x,y);
		
	}

	/* -------------------------------------------------------------------------- */
	
	// EDGE GETTERS //
	// ------------ //
	
	public double getLeft(double velocityX) {
		
		return super.getX() - velocityX;
		
	}
	public double getTop(double velocityY) {

		return super.getY() - velocityY;
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// HELPER FUNCTIONS //
	// ---------------- //
	
	//Checks if the camera renders outside the range of the tiledMap
	
	public boolean outsideX(double velocityX) {
		
		return !((LEFT_EDGE + WINDOW_WIDTH <= this.getLeft(velocityX)) && 
				(this.getLeft(velocityX) <= RIGHT_EDGE));
		
	}
	
	public boolean outsideY(double velocityY) {
		
		return !((TOP_EDGE + WINDOW_HEIGHT <= this.getTop(velocityY)) && 
				(this.getTop(velocityY) <= BOTTOM_EDGE));
		
	}

	/* ========================================================================== */

}