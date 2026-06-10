package pl.kacper.musclelab.exception.validation;

public class WrongTrainingDataException extends RuntimeException {
    public WrongTrainingDataException() {
        super("Date of training is incorrect");
    }
}
