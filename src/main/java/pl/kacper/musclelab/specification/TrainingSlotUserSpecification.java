package pl.kacper.musclelab.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kacper.musclelab.dto.filter.TrainingSlotUserFilter;
import pl.kacper.musclelab.model.TrainingSlot;

import java.util.ArrayList;
import java.util.List;

public class TrainingSlotUserSpecification {

    public static Specification<TrainingSlot> filter(TrainingSlotUserFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getCoachUsername() != null && !filter.getCoachUsername().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("coach").get("username")),
                        "%" + filter.getCoachUsername().toLowerCase() + "%"
                ));
            }

            if (filter.getExactCoachUsername() != null && !filter.getExactCoachUsername().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("coach").get("username")),
                                filter.getExactCoachUsername().toLowerCase()
                ));
            }

            if (filter.getCoachSpecialisation() != null && !filter.getCoachSpecialisation().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("coach").get("specialisation")),
                        filter.getCoachSpecialisation().toLowerCase()
                ));
            }

            if (filter.getMinAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("coach").get("age"),
                        filter.getMinAge()
                ));
            }

            if (filter.getMaxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("coach").get("age"),
                        filter.getMaxAge()
                ));
            }

            if (filter.getMinExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("coach").get("experience"),
                        filter.getMinExperience()
                ));
            }

            if (filter.getMaxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("coach").get("experience"),
                        filter.getMaxExperience()
                ));
            }

            if (filter.getStartFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("startTime"),
                        filter.getStartFrom()
                ));
            }

            if (filter.getStartTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("startTime"),
                        filter.getStartTo()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
