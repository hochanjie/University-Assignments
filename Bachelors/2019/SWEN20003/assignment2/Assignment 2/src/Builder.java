import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 * Builder class which can create factories
 */

public class Builder extends Units {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String BUILDER_IMAGE = "assets/units/builder.png";
    private static final double BUILDER_SPEED = 0.1;
    private static final int FACTORY_COST = 10;
    private static final int FINISHED = 0;
    private static final int TIME_TO_CREATE = 10000;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double timeLeft = TIME_TO_CREATE;
    private boolean isCreating = false;


    /* ============================================================================================================== */

    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * Builder Constructor that sets the coordinates, image, and speed
     *
     * @param x The x-coordinate of the builder
     * @param y The x-coordinate of the builder
     *
     * @throws SlickException
     */

    public Builder(double x, double y) throws SlickException {

        super(x,y, BUILDER_IMAGE, BUILDER_SPEED);

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Prints what the Builder can do and which buttons need to be pressed
     *
     * @return The instructions
     */

    @Override
    public String toString() {

        return "1- Create Factory";

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to create the Factory
     *
     * @param world World class which stores last input, delta, and the Buildings list
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        Input input = world.getInput();

        super.update(world);

        // Can only create one at a time at an unoccupied tile, so make sure is not currently creating anything

        if (!isCreating && world.getSelected() == this) {

            if (input.isKeyDown(Input.KEY_1) && world.isPositionOccupied(getX(), getY())) {

                // Only reset the timer if we collected enough metal

                if (world.getTotalMetal() >= FACTORY_COST) {

                    isCreating = true;
                    world.changeTotalMetal(-FACTORY_COST);
                    timeLeft = TIME_TO_CREATE;

                }

            }


        }

        if (isCreating) {

            // Cannot move while creating so keep resetting target

            setTarget(getX(), getY());
            timeLeft -= world.getDelta();

            if (timeLeft <= FINISHED) {

                world.getBuildings().add(new Factory(getX(), getY()));
                isCreating = false;

            }

        }

        super.update(world);

    }

    /* ============================================================================================================== */

}
