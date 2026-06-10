package pl.kacper.musclelab.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kacper.musclelab.dto.filter.TrainingSlotCoachFilter;
import pl.kacper.musclelab.model.TrainingSlot;

import java.util.ArrayList;
import java.util.List;

public class TrainingSlotCoachSpecification {
    public static Specification<TrainingSlot> filter(String coachUsername, TrainingSlotCoachFilter filter){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(
                    cb.lower(root.get("coach").get("username")),
                    coachUsername.toLowerCase()
            ));

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(
                        root.get("status"),
                        filter.getStatus()));
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

            return cb.and((predicates.toArray(new Predicate[0])));
        };
    }
}
