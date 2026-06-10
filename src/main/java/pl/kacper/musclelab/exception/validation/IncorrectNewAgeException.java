package pl.kacper.musclelab.exception.validation;

public class IncorrectNewAgeException extends RuntimeException {
    public IncorrectNewAgeException(Integer newAge) {
        super("Age is beyond the limit: " + newAge + " Age (10 - 100)");
    }
}
