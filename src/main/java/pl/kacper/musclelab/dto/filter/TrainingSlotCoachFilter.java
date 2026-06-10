package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.SlotStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class TrainingSlotCoachFilter {
    private SlotStatus status;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;
}
