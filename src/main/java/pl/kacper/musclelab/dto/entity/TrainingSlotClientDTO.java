package pl.kacper.musclelab.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.SlotStatus;

import java.time.LocalDateTime;

// Client widzi dostępne terminy trenerów / dla każdego trenera
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TrainingSlotClientDTO {
    private String slotCode;
    private String coachUsername;
    private String coachEmail;
    private String coachFirstName;
    private String coachSpecialisation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SlotStatus status;
}
