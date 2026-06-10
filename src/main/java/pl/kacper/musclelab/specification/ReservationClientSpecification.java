package pl.kacper.musclelab.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kacper.musclelab.dto.filter.ReservationClientFilter;
import pl.kacper.musclelab.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationClientSpecification {

    public static Specification<Reservation> filter(String clientUsername, ReservationClientFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    cb.lower(root.get("client").get("username")),
                    clientUsername.toLowerCase()
            ));

            if (filter.getTrainingSlotCode() != null && !filter.getTrainingSlotCode().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("trainingSlot").get("slotCode")),
                        filter.getTrainingSlotCode().toLowerCase()
                ));
            }

            if (filter.getCoachUsername() != null && !filter.getCoachUsername().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("trainingSlot").get("coach").get("username")),
                        "%" + filter.getCoachUsername().toLowerCase() + "%"
                ));
            }

            if (filter.getCoachExperienceFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("trainingSlot").get("coach").get("experience"),
                        filter.getCoachExperienceFrom()
                ));
            }

            if (filter.getTrainingStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("trainingSlot").get("startTime"),
                        filter.getTrainingStartFrom()
                ));
            }

            if (filter.getTrainingStartTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("trainingSlot").get("startTime"),
                        filter.getTrainingStartTo()
                ));
            }

            if (filter.getReservationStatus() != null) {
                predicates.add(cb.equal(
                        root.get("status"),
                        filter.getReservationStatus()
                ));
            }

            if (filter.getCreatedAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filter.getCreatedAtFrom()
                ));
            }

            if (filter.getCreatedAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        filter.getCreatedAtTo()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}