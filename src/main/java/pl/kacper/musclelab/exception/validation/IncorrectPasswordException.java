package pl.kacper.musclelab.exception.validation;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Password is incorrect");
    }
}
