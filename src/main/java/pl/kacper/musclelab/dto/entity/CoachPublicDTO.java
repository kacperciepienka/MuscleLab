package pl.kacper.musclelab.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Client ma opcje oglądania trenerów i po wejściu na ich profil ma opcje zobaczenia ich grafików
@Getter
@Setter
@AllArgsConstructor
@Builder
public class CoachPublicDTO {
    private String username; // do przeglądania trenerów
    private String email; // do wysyłania maila do trenera
    private String firstName;
    private String lastName;
    private Integer age;
    private Integer experience;
    private String bio; // może być brak bio
    private String specialisation; // może być brak specjalizacji
}
