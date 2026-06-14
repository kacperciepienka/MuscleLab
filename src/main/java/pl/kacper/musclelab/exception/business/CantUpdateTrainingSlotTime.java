package pl.kacper.musclelab.exception.business;

public class CantUpdateTrainingSlotTime extends RuntimeException {
    public CantUpdateTrainingSlotTime(String slotCode) {
        super("Can not update training slot " + slotCode + ". It is too close to training start for update ");
    }
}
