package pl.kacper.musclelab.dto.mainView;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserMainViewDTO {
    private String userId;
    private String username;
    private String email; // do wysyłania maili
    private String firstName;
    private String lastName;
    private Integer age;
    private Integer experience;
    private String bio; // może być brak bio
}
