public class GameObject{
	
	private double x, y; // nEvER uSE sINgLe lEtTeR VaRiAbLe NaMEs
	
	/* ========================================================================== */
	
	// GAMEOBJECT CONSTRUCTOR //
	// ---------------------- //
	
	public GameObject(double x, double y){
		
		this.x = x;
		this.y = y;
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// COORDINATE GETTERS //
	// ------------------ //
	
	public double getX() {
		
		return this.x;
		
	}
	
	public double getY() {
		
		return this.y;
		
	}
	
	/* -------------------------------------------------------------------------- */
	
	// COORDINATE SETTERS //
	// ------------------ //
	
	public void setX(double x) {
		
		this.x = x;
		
	}
	
	public void setY(double y) {
		
		this.y = y;
		
	}
	
	/* ========================================================================== */
		
}