package pl.kacper.musclelab.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kacper.musclelab.dto.filter.ReservationCoachFilter;
import pl.kacper.musclelab.model.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationCoachSpecification {

    public static Specification<Reservation> filter(String coachUsername, ReservationCoachFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    cb.lower(root.get("trainingSlot").get("coach").get("username")),
                    coachUsername.toLowerCase()
            ));

            if (filter.getClientUsername() != null && !filter.getClientUsername().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("client").get("username")),
                        "%" + filter.getClientUsername().toLowerCase() + "%"
                ));
            }

            if (filter.getClientAgeFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("client").get("age"),
                        filter.getClientAgeFrom()
                ));
            }

            if (filter.getClientAgeTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("client").get("age"),
                        filter.getClientAgeTo()
                ));
            }

            if (filter.getClientExperienceFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("client").get("experience"),
                        filter.getClientExperienceFrom()
                ));
            }

            if (filter.getClientExperienceTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("client").get("experience"),
                        filter.getClientExperienceTo()
                ));
            }

            if (filter.getTrainingSlotCode() != null && !filter.getTrainingSlotCode().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("trainingSlot").get("slotCode")),
                        filter.getTrainingSlotCode().toLowerCase()
                ));
            }

            if (filter.getStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("trainingSlot").get("startTime"),
                        filter.getStartFrom()
                ));
            }

            if (filter.getStartTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("trainingSlot").get("startTime"),
                        filter.getStartTo()
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