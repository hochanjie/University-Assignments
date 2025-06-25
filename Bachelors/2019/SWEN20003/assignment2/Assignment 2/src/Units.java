import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 * Abstract Units class to group all the movable units
 */

public abstract class Units extends GameObject {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String HIGHLIGHT = "assets/highlight.png";
    private static final double STOPPING_DISTANCE = 0.25;
    private static final int MINIMUM_DISTANCE = 32;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double targetX;
    private double targetY;
    private double speed;


    /* ============================================================================================================== */

    /* CONSTRUCTOR, GETTERS, AND SETTERS */
    /* --------------------------------- */

    /**
     * Units constructor to set the image, highlight type, coordinates, and speed
     *
     * @param x The x-coordinates of the unit
     * @param y The y-coordinates of the unit
     * @param image The image to be rendered
     * @param speed The speed at which the unit can travel
     *
     * @throws SlickException
     */

    public Units(double x, double y, String image, double speed) throws SlickException {

        setImage(new Image(image));
        setHighlight(new Image(HIGHLIGHT));
        setCoordinates(x, y);
        setSpeed(speed);
        setTarget(getX(), getY());

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the targeted x-coordinate that the unit is traveling to
     *
     * @return Targeted x-coordinate
     */

    public double getTargetX() {

        return targetX;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Get the targeted y-coordinate that the unit is traveling to
     *
     * @return Targeted y-coordinate
     */

    public double getTargetY() {

        return targetY;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Set the targeted coordinates that the Units should travel to
     *
     * @param targetX Targeted x-coordinate
     * @param targetY Targeted y-coordinate
     */

    // Cause I'm far too lazy to type "setTarget" twice when its both going to be altered at the same time
    public void setTarget(double targetX, double targetY) {

        this.targetX = targetX;
        this.targetY = targetY;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Stops the Units from moving by resetting the targeted coordinates to the current coordinates
     */

    private void resetTarget() {

        setTarget(getX(), getY());

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Set the speed at which the Units travel to its target coordinates
     *
     * @param speed Speed of the Units in pixels/millisecond
     */

    public void setSpeed(double speed) {

        this.speed = speed;

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Update function which deals with updating the coordinates of the Units to represent movement as well as checking
     * if any unit is close enough to a Pylon to activate it
     *
     * @param world World class that stores the last input and delta
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        // Check if any of the pylons need activating first

        for (GameObject object: world.getBuildings()) {

            if (object instanceof Pylon) {

                if (world.distance(getX(), getY(), object.getX(), object.getY()) <= MINIMUM_DISTANCE) {

                    if (!((Pylon) object).isActivated()) {

                        ((Pylon) object).activate(world);

                    }

                }

            }

        }

        // If we're close to our target, reset to our current position

        if (world.distance(getX(), getY(), targetX, targetY) <= STOPPING_DISTANCE) {

            resetTarget();

        }
        else {

            // Calculate the appropriate x and y distances

            double theta = Math.atan2(targetY - getY(), targetX - getX());
            double dx = Math.cos(theta) * world.getDelta() * speed;
            double dy = Math.sin(theta) * world.getDelta() * speed;

            // Check the tile is free before moving; otherwise, we stop moving

            if (world.isPositionFree(getX() + dx, getY() + dy)) {

                setCoordinates(getX() + dx, getY() + dy);

            }
            else {

                resetTarget();

            }

        }

    }

    /* ============================================================================================================== */

}
