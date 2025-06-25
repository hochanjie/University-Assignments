package database;

import java.util.ArrayList;

public class UserRegistry {

    public enum Status { FAILURE, MANAGER, SUCCESS }

    private static UserRegistry instance = null;

    private static User manager = null;
    private ArrayList<User> currentUsers;

    private UserRegistry() {
        this.currentUsers = new ArrayList<User>();
    }

    public static UserRegistry getInstance() {
        if (instance == null) {
            instance = new UserRegistry();
        }
        return instance;
    }

    public User getManager() {
        return manager;
    }

    public ArrayList<User> getCurrentUsers() {
        return currentUsers;
    }

    public Status addUser(User newUser) {

        if (exists(newUser.getUserName())) {
            return Status.FAILURE;
        }
        else {
            System.out.println(newUser.getUserName() + " successfully registered.");

            if (currentUsers.isEmpty()) {
                System.out.println(newUser.getUserName() + " is the manager");
                newUser.makeManager();
                manager = newUser;

                currentUsers.add(newUser);
                return Status.MANAGER;
            }
            else {
                currentUsers.add(newUser);
                return Status.SUCCESS;
            }
        }
    }

    public User removeUser(String username) {

        User found = null;

        System.out.println("Removing " + username + " from  the database");

        for (User currentUser: currentUsers) {
            if (currentUser.getUserName().equals(username)) {
                found = currentUser;
            }
        }
        if (found != null) {
            currentUsers.remove(found);
        }
        else {
            System.out.println(username + " does not exist in the database");
        }
        return found;
    }

    private boolean exists(String username) {
        if (!currentUsers.isEmpty()) {
            for (User user: currentUsers) {
                if (user.getUserName().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }
}
