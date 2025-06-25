import org.newdawn.slick.SlickException;

/**
 * Engineer class which can create factories
 */

public class Engineer extends Units {

    /* STATIC FINAL CONSTANTS */
    /* ---------------------- */

    private static final String ENGINEER_IMAGE = "assets/units/engineer.png";
    private static final double ENGINEER_SPEED = 0.1;
    private static final double STOPPING_DISTANCE = 0.25;
    private static final int MAXIMUM_DISTANCE = 32;
    private static final int MINING_TIME = 5000;
    private static final int FINISHED = 0;
    private static final int MAX_RANGE = 1000000000;


    /* ============================================================================================================== */

    /* INSTANCE VARIABLES */
    /* ------------------ */

    private int miningTime = MINING_TIME;
    private int load;
    private boolean loadIsMetal;
    private boolean isCarrying = false;

    private Resources currentMine = null;
    private GameObject nearestCommCentre = null;

    /* ============================================================================================================== */

    /* CONSTRUCTOR, AND SETTERS */
    /* ------------------------ */

    /**
     * Engineer constructor to set the coordinates, image, and speed
     *
     * @param x The x-coordinates of the Engineer
     * @param y The y-coordinates of the Engineer
     *
     * @throws SlickException
     */

    public Engineer(double x, double y) throws SlickException {

        super(x,y, ENGINEER_IMAGE, ENGINEER_SPEED);

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Set the mine that the engineer is currently mining
     *
     * @param currentMine The mine
     */

    public void setCurrentMine(Resources currentMine) {

        this.currentMine = currentMine;

    }


    /* ============================================================================================================== */

    /* HELPER METHODS */
    /* -------------- */

    /**
     * Function that the engineer would use to collect the resources of a mine after mining it for 5 seconds
     *
     * @param world World class that we get the amount of time passed, carrying capacity of Engineer, and Resources
     * @param mine The current mine that the engineer is mining
     */

    public void mine(World world, Resources mine) {

        // Check if the engineer has been mining the same mine

        if (currentMine != null && mine == currentMine) {

            miningTime -= world.getDelta();

            // Once time is up, find the closest command centre to drop the load off

            if (miningTime <= FINISHED) {

                isCarrying = true;
                load = world.getCarryingCapacity();

                closestCommCentre(world);
                setTarget(nearestCommCentre.getX(), nearestCommCentre.getY());

                // Guard for when the resource has less than the carrying capacity

                if (mine.getAmount() < load) {

                    load = mine.getAmount();

                }

                // Remember the resource type of the load

                loadIsMetal = currentMine.isMetal();
                currentMine.reduceAmount(load);

                if (currentMine.getAmount() == FINISHED) {

                    world.getResources().remove(currentMine);
                    setCurrentMine(null);

                }

            }

        }

        else {

            currentMine = mine;
            miningTime = MINING_TIME;

        }

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to determine which is the closest mine – which would be the mine that the engineer would mine
     *
     * @param world World class that we would get the function to calculate the distance
     *
     * @return The closest mine within range of the engineer (might be different to current mine)
     */

    public Resources closestMine(World world) {

        Resources closest = null;
        Camera camera = world.getCamera();

        // Iterate through all the mines in the worth and find the closest one within  range of the Engineer

        for (Resources mine: world.getResources()) {

            if (mine != null) {

                double distance = world.distance(mine.getX(), mine.getY(), getX(), getY());

                // Only compare the ones within range, and return the one with the smallest distance

                if (distance < MAXIMUM_DISTANCE) {

                    if (closest == null) {

                        closest = mine;

                    }
                    else {

                        if (distance < world.distance(camera.globalXToScreenX(closest.getX()),
                                camera.globalYToScreenY(closest.getY()), getX(), getY())) {

                            closest = mine;

                        }

                    }
                }

            }

        }

        return closest;

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function to find the closest Command Centre that the engineer would drop its load off at
     *
     * @param world World class that we would get the function to calculate the distance
     */

    public void closestCommCentre(World world) {

        // Initialise the minimum to be a crazy, impossibly large number

        Camera camera = world.getCamera();
        double minimum = MAX_RANGE;

        // Iterate through all the Command Centres to find the closest one

        for (GameObject object : world.getBuildings()) {

            if (object instanceof CommCentre) {

                double distance = world.distance(camera.globalXToScreenX(object.getX()),
                        camera.globalYToScreenY(object.getY()), getX(), getY());

                if (nearestCommCentre == null) {

                    minimum = distance;
                    nearestCommCentre = object;

                }
                else {

                    if (distance < minimum) {

                        minimum = distance;
                        nearestCommCentre = object;

                    }

                }

            }

        }

    }


    /* -------------------------------------------------------------------------------------------------------------- */

    /**
     * Function that would allow the engineer to mine a resource, find the closest command centre and travel towards it,
     * allowing for path alterations, drop the mined resources off, increase the total amount and then move back
     * to the resource
     *
     * @param world World class
     *
     * @throws SlickException
     */

    public void update(World world) throws SlickException {

        super.update(world);

        if (!isCarrying) {

            // This is not assigned to the variable currentMine as we want to check if it's the same mine

            Resources closestMine =  closestMine(world);
            mine(world, closestMine);

        }

        else {

            // Continue straight to nearest command centre but guard for when the Engineer was moving to a different
            // spot by left click on the way but not anymore.

            // Let Engineer move to clicked spot then continue moving to the nearest command centre from that location

            if (getTargetX() != nearestCommCentre.getX() || getTargetY() != nearestCommCentre.getY()) {

                if (world.distance(getTargetX(), getTargetY(), getX(), getY()) < STOPPING_DISTANCE) {

                    closestCommCentre(world);
                    setTarget(nearestCommCentre.getX(), nearestCommCentre.getY());

                }

            }

            // Reached the comm centre

            if (world.distance(nearestCommCentre.getX(), nearestCommCentre.getY(), getX(), getY()) < STOPPING_DISTANCE) {

                isCarrying = false;

                // Check which type of resource Engineer had been carrying

                if (loadIsMetal) {

                    world.changeTotalMetal(load);

                }
                else {

                    world.increaseTotalUnobtainium(load);

                }

                // Go back to the mine if it hasn't been depleted yet

                if (currentMine != null) {

                    setTarget(currentMine.getX(), currentMine.getY());

                }

            }

        }

    }

    /* ============================================================================================================== */

}
