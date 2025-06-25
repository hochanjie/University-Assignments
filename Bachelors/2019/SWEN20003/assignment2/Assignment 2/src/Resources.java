import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Resources class for the Metal and Unobtainium mines
 */

public class Resources {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String METAL_IMAGE = "assets/resources/metal_mine.png";
    private static final String UNOBTAINIUM_IMAGE = "assets/resources/unobtainium_mine.png";
    private static final String METAL_MINE = "metal_mine";
    private static final int METAL_START = 500;
    private static final int UNOBTAINIUM_START = 50;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double x;
    private double y;
    private int amount;
    private boolean isMetal;
    private Image image;


    /* ============================================================================================================== */

    /* CONSTRUCTORS, GETTERS, AND SETTERS */
    /* ---------------------------------- */

    /**
     * Resources constructor to set the type, image, amount, and coordinates of the mine
     * @param type The type of Resources
     * @param x The x-coordinate of the Resources mine
     * @param y The y-coordinate of the Resources mine
     * @throws SlickException
     */

    public Resources(String type, double x, double y) throws SlickException {

        if (type.equals(METAL_MINE)) {

            image = new Image(METAL_IMAGE);
            amount = METAL_START;
            isMetal = true;

        }
        else {

            image = new Image(UNOBTAINIUM_IMAGE);
            amount = UNOBTAINIUM_START;
            isMetal = false;

        }

        this.x = x;
        this.y = y;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    // Setters for coordinates, image, and type not created as they should not be able to change

    /**
     * Get the x-coordinates for the Resources mine
     *
     * @return The x-coordinates of the Resources mine
     */

    public double getX() {

        return x;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the y-coordinates for the Resources mine
     *
     * @return The y-coordinates of the Resources mine
     */

    public double getY() {

        return y;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the amount remaining in the Resources mine
     *
     * @return Amount remaining in the Resources mine
     */

    public int getAmount() {

        return amount;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Reduce the amount remaining in the Resources mine
     *
     * @param amount Amount to reduce the Resources mine by
     */

    public void reduceAmount(int amount) {

        this.amount -= amount;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Gives back the type of the Resources mine
     *
     * @return
     */

    public boolean isMetal() {

        return isMetal;

    }

    /* ============================================================================================================== */

    /* HELPER METHOD */
    /* ------------- */


    /**
     * Render function to draw the image of the Resources mine
     *
     * @param camera Camera function which helps convert the global coordinates of to ones relative to the screen
     */

    public void render(Camera camera) {

        image.drawCentered((float) camera.globalXToScreenX(x), (float) camera.globalYToScreenY(y));

    }

    /* ============================================================================================================== */

}
