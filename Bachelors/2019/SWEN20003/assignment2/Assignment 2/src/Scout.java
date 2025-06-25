import org.newdawn.slick.SlickException;

/**
 * Scout class which does not do anything but allows for simple coding when setting image and speed
 */

public class Scout extends Units {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String SCOUT_IMAGE = "assets/units/scout.png";
    private static final double SCOUT_SPEED = 0.3;

    /* ============================================================================================================== */

    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * Scout constructor that sets the image and speed of the Scout
     *
     * @param x The x-coordinates of the Scout
     * @param y The y-coordinates of the Scout
     *
     * @throws SlickException
     */

    public Scout(double x, double y) throws SlickException {

        super(x,y, SCOUT_IMAGE, SCOUT_SPEED);

    }

    /* ============================================================================================================== */

}
