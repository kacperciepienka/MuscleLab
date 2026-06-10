package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.ReservationStatus;
import java.time.LocalDateTime;

// widok swoich rezerwacji dla klienta co filtruje klient
@Getter
@Setter
public class ReservationClientFilter {
    // kto zarezerwował nie pisze nic, bo wiadomo kto (osoba zalogowana)

    // co zarezerwował
    private String trainingSlotCode;
    private String coachUsername;
    private Integer coachExperienceFrom;
    private LocalDateTime trainingStartFrom;
    private LocalDateTime trainingStartTo;

    private ReservationStatus reservationStatus;
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
}
