import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 * Factory class which can create trucks
 */

public class Factory extends GameObject {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String FACTORY_IMAGE = "assets/buildings/factory.png";
    private static final String HIGHLIGHT = "assets/highlight_large.png";

    private static final int TIME_TO_CREATE = 5000;
    private static final int TRUCK_COST = 150;
    private static final int FINISHED = 0;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double timeLeft = TIME_TO_CREATE;
    private boolean isCreating = false;


    /* ============================================================================================================== */

    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * Factory constructor that sets the image, highlight type, and coordinates
     *
     * @param x The x-coordinates of the Factory
     * @param y The y-coordinates of the Factory
     *
     * @throws SlickException
     */

    public Factory(double x, double y) throws SlickException {

        setImage(new Image(FACTORY_IMAGE));
        setHighlight(new Image(HIGHLIGHT));
        setCoordinates(x, y);

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Prints what the Factory can do and which buttons need to be pressed
     *
     * @return The instructions
     */

    @Override
    public String toString() {

        return "1- Create Truck";

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to create the Truck
     *
     * @param world World class which stores last input, delta, and Units list
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        Input input = world.getInput();

        if (!isCreating && world.getSelected() == this) {

            if (input.isKeyDown(Input.KEY_1)) {

                if (world.getTotalMetal() >= TRUCK_COST) {

                    isCreating = true;
                    world.changeTotalMetal(-TRUCK_COST);
                    timeLeft = TIME_TO_CREATE;

                }

            }

        }

        if (isCreating) {

            timeLeft -= world.getDelta();

            if (timeLeft <= FINISHED) {

                world.getUnits().add(new Truck(super.getX(), super.getY()));
                isCreating = false;

            }

        }

    }

    /* ============================================================================================================== */

}
