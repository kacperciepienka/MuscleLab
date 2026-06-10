package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.ReservationStatus;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationCoachFilter {
    //kto zarezerwował
    private String clientUsername;
    private Integer clientAgeFrom;
    private Integer clientAgeTo;
    private Integer clientExperienceFrom;
    private Integer clientExperienceTo;

    // Co zarezerwował-do wyszukiwania statystyk np.
    // widać czy raczej jestem trenerem necronym, czy porannym
    private String trainingSlotCode;
    private LocalDateTime startFrom;
    private LocalDateTime startTo;

    // kiedy
    private ReservationStatus reservationStatus;
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
}
