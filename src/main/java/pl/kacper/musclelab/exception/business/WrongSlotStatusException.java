package pl.kacper.musclelab.exception.business;

public class WrongSlotStatusException extends RuntimeException {
    public WrongSlotStatusException(String slotCode) {
        super("Slot: " + slotCode + " has incorrect status");
    }
}
