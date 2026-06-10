package pl.kacper.musclelab.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pl.kacper.musclelab.model.ReservationStatus;

import java.time.LocalDateTime;

// widok rezerwacji dla klienta
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReservationClientDTO {
    private String reservationCode;
    // kto zarezerwował w ramach potwierdzenia
    private String reservationClientUsername;
    private String reservationClientEmail;
    private String reservationClientFirstName;
    // co zarezerwował
    private String reservationTrainingSlotCode;
    private String reservationTrainingSlotCoachUsername;
    private String reservationTrainingSlotCoachEmail;
    private String reservationTrainingSlotCoachFirstName;
    private Integer reservationTrainingSlotCoachExperience; // <- widzi poziom doświadczenia trenera
    private String reservationTrainingSlotCoachSpecialisation; // <- widzi specjalizacje trenera
    private LocalDateTime reservationTrainingSlotStartTime;
    private LocalDateTime reservationTrainingSlotEndTime;

    // parametry rezerwacji
    private ReservationStatus status;
    private LocalDateTime createdAt;
}
