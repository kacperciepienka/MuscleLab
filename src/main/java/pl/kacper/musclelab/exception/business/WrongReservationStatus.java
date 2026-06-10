package pl.kacper.musclelab.exception.business;

public class WrongReservationStatus extends RuntimeException {
    public WrongReservationStatus(String reservationCode) {
        super("Reservation: " + reservationCode + " has wrong status");
    }
}
