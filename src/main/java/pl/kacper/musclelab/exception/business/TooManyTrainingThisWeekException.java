package pl.kacper.musclelab.exception.business;

public class TooManyTrainingThisWeekException extends RuntimeException {
    public TooManyTrainingThisWeekException() {
        super("You reached maximum training units today (max. 10)");
    }
}
