package pl.kacper.musclelab.exception.validation;

public class IncorrectNewExperienceException extends RuntimeException {
    public IncorrectNewExperienceException(Integer newExperience) {
        super("Experience is beyond the limit: " + newExperience + " Age (0 - 100)");
    }
}
