package pl.kacper.musclelab.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateReservation {

    @NotBlank(message = "Training slot code can't be empty")
    private String trainingSlotCode;
}
