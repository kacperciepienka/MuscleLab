package pl.kacper.musclelab.exception.not_Found;

public class TrainingSlotNotFoundException extends RuntimeException {
    public TrainingSlotNotFoundException(String slotCode) {
        super("Training slot with slot code: " + slotCode + " does not exist");
    }
}
