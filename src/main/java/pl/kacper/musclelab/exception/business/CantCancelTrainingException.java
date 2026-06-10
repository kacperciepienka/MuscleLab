package pl.kacper.musclelab.exception.business;

public class CantCancelTrainingException extends RuntimeException {
    public CantCancelTrainingException() {
        super("Cancel training is impossible now. You can cancel training at least 12 hours before it starts ");
    }
}
