package pl.kacper.musclelab.exception.validation;

public class IncorrectNewEmailException extends RuntimeException {
    public IncorrectNewEmailException(String email) {
        super("New email is incorrect: " + email);
    }
}
