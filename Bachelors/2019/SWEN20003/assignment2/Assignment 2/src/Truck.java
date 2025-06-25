import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 * Truck class that can create Command Centres and is destroyed after
 */

public class Truck extends Units {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String TRUCK_IMAGE = "assets/units/truck.png";
    private static final double TRUCK_SPEED = 0.25;
    private static final int TIME_TO_CREATE = 15000;
    private static final int FINISHED = 0;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double timeLeft = TIME_TO_CREATE;
    private boolean isCreating = false;
    private boolean isDone = false;


    /* ============================================================================================================== */

    /* CONSTRUCTOR, AND GETTER */
    /* ----------------------- */

    /**
     * Truck constructor to set the coordinates, image, and speed of the Units
     *
     * @param x The x-coordinates of the Truck
     * @param y The y-coordinates of the Truck
     *
     * @throws SlickException
     */

    public Truck(double x, double y) throws SlickException {

        super(x,y, TRUCK_IMAGE, TRUCK_SPEED);

    }

    /* -------------------------------------------------------------------------------------------------------------- */


    /**
     * Checks if the truck has finished creating the command centre and should be destroyed
     *
     * @return Completion status of creation
     */

    public boolean isDone() {

        return isDone;

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Prints what the Truck can do and which buttons need to be pressed
     *
     * @return The instructions
     */

    @Override
    public String toString() {

        return "1- Create Command Centre";

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to create the Command Centre and destroy itself afterwards
     *
     * @param world World class which stores last input, delta, and the GameObject lists
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        Input input = world.getInput();

        // Since it is free, just check for the button press and for the occupied tile property

        if (!isCreating && world.getSelected() == this) {

            if (input.isKeyDown(Input.KEY_1) && world.isPositionOccupied(getX(), getY())) {

                isCreating = true;

            }

        }

        if (isCreating) {

            // Do not allow the truck to move while creating

            setTarget(getX(), getY());
            timeLeft -= world.getDelta();

            if (timeLeft <= FINISHED) {

                // Do not remove from units yet as that would throw a Concurrent Modification Error, but can add new
                // Command centre

                world.getBuildings().add(new CommCentre(getX(), getY()));
                isDone = true;

            }

        }

        super.update(world);

    }

    /* ============================================================================================================== */

}
