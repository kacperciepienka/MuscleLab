package pl.kacper.musclelab.exception.validation;

public class PasswordsAreNotTheSameException extends RuntimeException {
    public PasswordsAreNotTheSameException() {
        super("Passwords are not the same");
    }
}
