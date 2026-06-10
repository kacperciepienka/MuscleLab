package pl.kacper.musclelab.exception.not_Found;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("User with username: " + username + " does not exist");
    }
}
