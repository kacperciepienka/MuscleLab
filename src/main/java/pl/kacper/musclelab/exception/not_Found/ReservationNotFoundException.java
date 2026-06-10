package pl.kacper.musclelab.exception.not_Found;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String reservationCode) {
        super("Reservation with code: " + reservationCode + " does not exist");
    }
}
