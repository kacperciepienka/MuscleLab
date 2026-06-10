package pl.kacper.musclelab.exception.validation;

public class TooBigDifferenceBetweenOriginalDateException extends RuntimeException {
    public TooBigDifferenceBetweenOriginalDateException() {
        super("Maximum difference between original date is 2 hours earlier and 2 hours later");
    }
}
