package pl.kacper.musclelab.exception.conflict;

public class UserEmailAlreadyExist extends RuntimeException {
    public UserEmailAlreadyExist(String email) {
        super("This email is already taken: " + email);
    }
}
