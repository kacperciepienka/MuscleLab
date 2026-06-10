package pl.kacper.musclelab.exception.business;

public class TooManyTrainingTodayException extends RuntimeException {
    public TooManyTrainingTodayException() {
        super("You reached maximum training units today (max. 3)");
    }
}
