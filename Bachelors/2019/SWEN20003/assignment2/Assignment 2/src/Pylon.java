import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Pylon class which can increase the carrying capacity of the Engineers after being activated
 */

public class Pylon extends GameObject {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String PYLON_IMAGE = "assets/buildings/pylon.png";
    private static final String ACTIVATED_PYLON_IMAGE = "assets/buildings/pylon_active.png";
    private static final String HIGHLIGHT = "assets/highlight_large.png";


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private boolean isActivated = false;


    /* ============================================================================================================== */

    /* CONSTRUCTORS, GETTERS, AND SETTERS */
    /* ---------------------------------- */

    /**
     * Pylon constructor that sets the image, highlight type, and coordinates
     *
     * @param x The x-coordinates of the Pylon
     * @param y The y-coordinates of the Pylon
     *
     * @throws SlickException
     */

    public Pylon(double x, double y) throws SlickException {

        setImage(new Image(PYLON_IMAGE));
        setHighlight(new Image(HIGHLIGHT));
        setCoordinates(x, y);

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Checks if the Pylon has already been activated
     *
     * @return Activation status of Pylon
     */

    public boolean isActivated() {

        return isActivated;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Activates the Pylon, which is essentially changing the image, and activation status, and increasing the
     * carrying capacity of the Engineers
     *
     * @param world World class that stores the carrying capacity of the Engineers
     *
     * @throws SlickException
     */

    public void activate(World world) throws SlickException {

        setImage(new Image(ACTIVATED_PYLON_IMAGE));
        world.increaseCarryingCapacity();
        isActivated = true;

    }

    /* ============================================================================================================== */

}
