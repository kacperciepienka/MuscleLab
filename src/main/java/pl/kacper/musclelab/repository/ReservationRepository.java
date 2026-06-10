package pl.kacper.musclelab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kacper.musclelab.model.Reservation;
import pl.kacper.musclelab.model.ReservationStatus;
import pl.kacper.musclelab.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findReservationByReservationCodeEqualsIgnoreCase(String reservationCode);

    long countByClientAndStatusAndTrainingSlot_StartTimeBetween(
            User client,
            ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByClientAndStatusAndTrainingSlot_StartTimeAfter(
            User client,
            ReservationStatus status,
            LocalDateTime now
    );

    @Modifying
    @Query(value = "DELETE FROM reservation WHERE client_id = :clientId", nativeQuery = true)
    void deleteAllByClientId(@Param("clientId") Long clientId);

    @Modifying
    @Query(value = """
            DELETE FROM reservation
            WHERE training_slot_id IN (
                SELECT id FROM training_slot WHERE coach_id = :coachId
            )
            """, nativeQuery = true)
    void deleteAllByCoachId(@Param("coachId") Long coachId);
}