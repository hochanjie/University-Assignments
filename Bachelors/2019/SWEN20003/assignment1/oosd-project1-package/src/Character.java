import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Character extends GameObject implements Movable {
	
	// CONSTANTS AND INSTANCE VARIABLES //
	// -------------------------------- //
	
	public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final int MIDDLE = 2;
    public static final int STOP = 0;
	
	private Image image;
		
	/* ========================================================================== */
	
	// CHARACTER CONSTRUCTOR //
	// --------------------- //
	
	public Character(String ref, double x, double y) throws SlickException {
		
		super(x, y);
		this.image = new Image(ref);	
	
	}
	
	/* -------------------------------------------------------------------------- */
	
	// WORLD COORDINATE GETTERS //
	// ------------------------ //
	
	public double getWorldX(Camera camera, double velocityX) {
		
		return super.getX() - camera.getLeft(velocityX);
		
	}
	
	public double getWorldY(Camera camera, double velocityY) {
		
		return super.getY() - camera.getTop(velocityY);
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// HELPER FUNCTIONS //
	// ---------------- //
	
	// Checks if player is centred in the window
	
	public boolean notCentredX(double velocityX) {
		
		return ((velocityX < STOP && super.getX() > WINDOW_WIDTH/MIDDLE) || 
				(velocityX > STOP && super.getX() < WINDOW_WIDTH/MIDDLE));
		
	}
	
	public boolean notCentredY(double velocityY) {
		
		return ((velocityY < STOP && super.getY() > WINDOW_HEIGHT/MIDDLE) || 
				(velocityY > STOP && super.getY() < WINDOW_HEIGHT/MIDDLE));
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// CHARACTER RENDER //
	// ---------------- //
	
	public void render() throws SlickException {
		
		this.image.draw((float) super.getX(), (float) super.getY()) ;	
		
	}
	
	/* ========================================================================== */
	
}