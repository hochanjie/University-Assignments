import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/**
 * Command Centre class that can create scouts, builders, and engineers using metal
 */

public class CommCentre extends GameObject {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String COMM_CENTRE_IMAGE = "assets/buildings/command_centre.png";
    private static final String HIGHLIGHT = "assets/highlight_large.png";

    private static final int FINISHED = 0;
    private static final int SCOUT_COST = 5;
    private static final int BUILDER_COST = 10;
    private static final int ENGINEER_COST = 20;
    private static final int TIME_TO_CREATE = 5000;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private double timeLeft = TIME_TO_CREATE;
    private boolean isCreating = false;
    private int cost;


    /* ============================================================================================================== */

    /* CONSTRUCTOR */
    /* ----------- */

    /**
     * Command Centre constructor that will set the image, highlight type, and coordinates
     *
     * @param x The x-coordinates
     * @param y The y-coordinates
     *
     * @throws SlickException
     */

    public CommCentre(double x, double y) throws SlickException {

        setImage(new Image(COMM_CENTRE_IMAGE));
        setHighlight(new Image(HIGHLIGHT));
        setCoordinates(x, y);

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Prints what the Command Centre can do and which buttons need to be pressed
     *
     * @return The instructions
     */

    @Override
    public String toString() {

        return "1- Create Scout\n" +
                "2- Create Builder\n" +
                "3- Create Engineer\n";

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to create the units after a certain waiting time
     *
     * @param world World class which stores last input, delta, and Units list
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        Input input = world.getInput();

        // Can only create one unit at a time

        if (!isCreating && world.getSelected() == this) {

            // Only charge and start creation of unit if user presses button AFTER there's enough metal to pay for it

            boolean clicked = false;

            if (input.isKeyDown(Input.KEY_1)) {

                clicked = true;
                cost = SCOUT_COST;

            }

            else if (input.isKeyDown(Input.KEY_2)) {

                clicked = true;
                cost = BUILDER_COST;

            }

            else if (input.isKeyDown(Input.KEY_3)) {

                clicked = true;
                cost = ENGINEER_COST;

            }

            if (clicked && world.getTotalMetal() >= cost) {

                isCreating = true;
                world.changeTotalMetal(-cost);
                timeLeft = TIME_TO_CREATE;

            }


        }

        if (isCreating) {

            timeLeft -= world.getDelta();

            if (timeLeft <= FINISHED) {

                // Must find which unit for which we were charged for

                switch (cost) {

                    case SCOUT_COST:

                        world.getUnits().add(new Scout(getX(), getY()));
                        break;

                    case BUILDER_COST:

                        world.getUnits().add(new Builder(getX(), getY()));
                        break;

                    case ENGINEER_COST:

                        world.getUnits().add(new Engineer(getX(), getY()));
                        break;

                }

                isCreating = false;

            }

        }

    }

    /* ============================================================================================================== */

}
