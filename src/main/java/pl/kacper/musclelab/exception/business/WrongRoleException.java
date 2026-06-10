package pl.kacper.musclelab.exception.business;

public class WrongRoleException extends RuntimeException {
    public WrongRoleException() {
        super("Your role is not correct");
    }
}
