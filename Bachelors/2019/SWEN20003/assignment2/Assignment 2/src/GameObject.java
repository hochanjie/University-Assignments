import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Abstract GameObject class to group together Units and Buildings (not a class) that the user can interact with,
 * such as selecting, creating, etc.
 */

public abstract class GameObject {

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double x;
    private double y;
    private Image image;
    private Image highlight;


    /* ============================================================================================================== */

    /* GETTERS, AND SETTERS */
    /* -------------------- */

    /**
     * Get the x-coordinates of a GameObject
     *
     * @return The x-coordinates
     */

    public double getX() {

        return x;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the y-coordinates of a GameObject
     *
     * @return The y-coordinates
     */

    public double getY() {

        return y;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Sets the coordinates of a GameObject
     *
     * @param x The x-coordinates
     * @param y The y-coordinates
     */

    // Also because I'm too lazy to write 2 set functions when they are both always altered at the same time

    public void setCoordinates(double x, double y) {

        this.x = x;
        this.y = y;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the image of a GameObject that would be rendered
     *
     * @return The image
     */

    public Image getImage() {

        return image;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Set the image that the GameObject would render
     *
     * @param image The image to be rendered
     */

    public void setImage(Image image) {

        this.image = image;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the image of the highlight of a GameObject that would be shown when selected
     *
     * @return The image of the highlight
     */

    public Image getHighlight() {

        return highlight;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Sets the image of the highlight that would be under a GameObject when it is highlighted
     *
     * @param highlight The image of the highlight
     */

    public void setHighlight(Image highlight) {

        this.highlight = highlight;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Target Coordinates setter which is empty as it would be later overridden by Units
     *
     * @param targetX Targeted x-coordinates
     * @param targetY Targeted y-coordinates
     */

    public void setTarget(double targetX, double targetY) {

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Prints what the GameObject can do and which buttons need to be pressed
     *
     * @return The instructions / Empty string for those without any actions
     */

    @Override
    public String toString() {

        return "";

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Update function which is empty as it would be later overridden by Units
     *
     * @param world World class that stores the last input and delta
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Render function that renders the image of the GameObject
     *
     * @param camera Camera class that calculates the coordinates of a GameObject relative to the screen
     */

    public void render(Camera camera) {

        getImage().drawCentered((float) camera.globalXToScreenX(getX()), (float) camera.globalYToScreenY(getY()));

    }

    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Highlight function that renders the highlight of the GameObject when selected
     *
     * @param camera Camera class that calculates the coordinates of a GameObject relative to the screen
     */

    public void highlight(Camera camera) {

        getHighlight().drawCentered((float) camera.globalXToScreenX(getX()), (float) camera.globalYToScreenY(getY()));
    }

    /* ============================================================================================================== */

}
