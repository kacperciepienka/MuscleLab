package pl.kacper.musclelab.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateTrainingSlot {

    @NotNull(message = "Start time can't be empty")
    private LocalDateTime startTime;

    @NotNull(message = "End time can't be empty")
    private LocalDateTime endTime;
}
