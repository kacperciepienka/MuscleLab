package pl.kacper.musclelab.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Coach ma opcje oglądania wszystkich Client widok na wszystkich Client
@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserCoachDTO {
    private String username;
    private String email; // do wysyłania maili
    private String firstName;
    private String lastName;
    private Integer age;
    private Integer experience;
    private String bio; // może być brak bio
}
