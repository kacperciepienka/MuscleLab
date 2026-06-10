package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;

// dla klientów, którzy szukają trenera
@Getter
@Setter
public class CoachClientFilter {
    private String username;
    private String firstName;
    private String lastName;
    private Integer ageMin;
    private Integer ageMax;
    private Integer experienceMin;
    private Integer experienceMax;
    private String specialisation;
}
