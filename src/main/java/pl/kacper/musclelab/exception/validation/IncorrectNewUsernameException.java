package pl.kacper.musclelab.exception.validation;

public class IncorrectNewUsernameException extends RuntimeException {
    public IncorrectNewUsernameException(String username) {
        super("New username is incorrect: " + username);
    }
}
