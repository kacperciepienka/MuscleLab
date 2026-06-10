package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.SlotStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class TrainingSlotUserFilter {
    private SlotStatus status;
    private String coachUsername;
    private String exactCoachUsername;
    private String coachSpecialisation;
    private Integer minAge;
    private Integer maxAge;
    private Integer minExperience;
    private Integer maxExperience;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;
}

