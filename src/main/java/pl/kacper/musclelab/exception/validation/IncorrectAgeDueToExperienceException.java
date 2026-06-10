package pl.kacper.musclelab.exception.validation;

public class IncorrectAgeDueToExperienceException extends RuntimeException {
    public IncorrectAgeDueToExperienceException() {
        super("Age can not be lower than experience");
    }
}
