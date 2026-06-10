package pl.kacper.musclelab.exception.business;

public class CantCompleteTrainingBeforeItEndsException extends RuntimeException {
    public CantCompleteTrainingBeforeItEndsException() {
        super("Reservation which not finish yet. Can not be completed");
    }
}
