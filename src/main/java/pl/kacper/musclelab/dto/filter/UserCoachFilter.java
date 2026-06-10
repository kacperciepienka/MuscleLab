package pl.kacper.musclelab.dto.filter;

import lombok.Getter;
import lombok.Setter;

// dla trenerów, którzy szukają podopiecznych
@Getter
@Setter
public class UserCoachFilter {
    private String username;
    private String firstName;
    private String lastName;
    private Integer ageMin;
    private Integer ageMax;
    private Integer experienceMin;
    private Integer experienceMax;
}
