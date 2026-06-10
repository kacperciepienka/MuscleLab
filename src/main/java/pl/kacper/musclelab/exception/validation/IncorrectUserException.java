package pl.kacper.musclelab.exception.validation;

public class IncorrectUserException extends RuntimeException {
    public IncorrectUserException(String username) {
        super("Wrong user" + username);
    }
}
