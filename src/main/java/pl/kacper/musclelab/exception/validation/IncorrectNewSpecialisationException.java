package pl.kacper.musclelab.exception.validation;

public class IncorrectNewSpecialisationException extends RuntimeException {
    public IncorrectNewSpecialisationException(String specialisation) {
        super("New specialisation is incorrect: " + specialisation + " too short (min. 2 characters) or too long (max. 80 characters)");
    }
}
