public interface Movable {
	
	// Created this interface because not all GameObjects might be movable
	// E.g. World is technically a GameObject but it's not "movable"
	
	/* ========================================================================== */

	// COORDINATE GETTERS //
	// ------------------ //
	
	public double getX();
	
	public double getY();
	
	/* -------------------------------------------------------------------------- */
	
	// COORDINATE SETTERS //
	// ------------------ //
	
	public void setX(double x);

	public void setY(double y);
	
	/* -------------------------------------------------------------------------- */
	
	// GAME OBJECT MOVERS //
	// ------------------ //
	
	default void moveX(double velocityX) {
		
		this.setX(this.getX() + velocityX);
	
	}
	
	default void moveY(double velocityY) {
		
		this.setY(this.getY() + velocityY);
		
	}
	
	/* ========================================================================== */
	
}

