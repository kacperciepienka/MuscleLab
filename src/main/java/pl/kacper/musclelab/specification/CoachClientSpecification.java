package pl.kacper.musclelab.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pl.kacper.musclelab.dto.filter.CoachClientFilter;
import pl.kacper.musclelab.model.Role;
import pl.kacper.musclelab.model.User;

import java.util.ArrayList;
import java.util.List;

public class CoachClientSpecification {

    public static Specification<User> filter(CoachClientFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("role"), Role.COACH));

            if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("username")),
                        "%" + filter.getUsername().toLowerCase() + "%"
                ));
            }

            if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("firstName")),
                        "%" + filter.getFirstName().toLowerCase() + "%"
                ));
            }

            if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("lastName")),
                        "%" + filter.getLastName().toLowerCase() + "%"
                ));
            }

            if (filter.getAgeMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("age"),
                        filter.getAgeMin()
                ));
            }

            if (filter.getAgeMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("age"),
                        filter.getAgeMax()
                ));
            }

            if (filter.getExperienceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("experience"),
                        filter.getExperienceMin()
                ));
            }

            if (filter.getExperienceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("experience"),
                        filter.getExperienceMax()
                ));
            }

            if (filter.getSpecialisation() != null && !filter.getSpecialisation().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("specialisation")),
                        filter.getSpecialisation().toLowerCase()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}