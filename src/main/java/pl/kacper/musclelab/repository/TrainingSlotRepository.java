package pl.kacper.musclelab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kacper.musclelab.model.TrainingSlot;

import java.util.Optional;

public interface TrainingSlotRepository extends JpaRepository<TrainingSlot, Long>, JpaSpecificationExecutor<TrainingSlot> {

    Optional<TrainingSlot> findTrainingSlotBySlotCodeEqualsIgnoreCase(String slotCode);

    @Modifying
    @Query(value = """
            UPDATE training_slot
            SET status = 'AVAILABLE'
            WHERE id IN (
                SELECT training_slot_id
                FROM reservation
                WHERE client_id = :clientId
                AND status = 'BOOKED'
            )
            """, nativeQuery = true)
    void makeBookedSlotsAvailableForClient(@Param("clientId") Long clientId);

    @Modifying
    @Query(value = "DELETE FROM training_slot WHERE coach_id = :coachId", nativeQuery = true)
    void deleteAllByCoachId(@Param("coachId") Long coachId);
}