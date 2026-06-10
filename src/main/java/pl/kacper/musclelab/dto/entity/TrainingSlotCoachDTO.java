package pl.kacper.musclelab.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.SlotStatus;

import java.time.LocalDateTime;

// trener widzi tylko swój grafik i tylko nim może zarządzać
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TrainingSlotCoachDTO {
    private String slotCode;
    private String coachUsername;
    private String coachFirstName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SlotStatus status;
}
