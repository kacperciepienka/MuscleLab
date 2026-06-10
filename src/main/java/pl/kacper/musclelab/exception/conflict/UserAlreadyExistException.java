package pl.kacper.musclelab.exception.conflict;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String username) {
        super("This username is already taken: " + username);
    }
}
