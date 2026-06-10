package pl.kacper.musclelab.exception.validation;

public class IncorrectNewBioException extends RuntimeException {
    public IncorrectNewBioException() {
        super("New bio is too short (min. 20 characters) or too long (max. 1000 characters)");
    }
}
