package pl.kacper.musclelab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // kod liczbowy z losowych liczb (do identyfikacji konta)
    @Column(length = 80, unique = true, nullable = false)
    @Length(min = 5, max = 80, message = "User id can't be lesser than 5 and longer than 80 characters")
    private String userId;

    @NotBlank(message = "Username can't be empty")
    @Column(length = 100, unique = true, nullable = false)
    @Length(min = 3, max = 100, message = "Username can't be shorter than 3 and longer than 100 characters")
    private String username;

    @NotBlank(message = "Email can't be empty")
    @Email(message = "email must have correct form ....@example.com ")
    @Column(length = 100, unique = true, nullable = false)
    @Length(min = 5, max = 100, message = "Email can't be shorter than 5 and longer than 100 characters")
    private String email;

    @NotBlank(message = "Password can't be empty")
    @Column(nullable = false)
    @Length(min = 6, max = 255, message = "Password can't be shorter than 6 and longer than 255 characters")
    private String password;

    @NotBlank(message = "First name can't be empty")
    @Column(length = 40, nullable = false)
    @Length(min = 2, max = 40, message = "First name can't be shorter than 2 and longer than 40 characters")
    private String firstName;

    @NotBlank(message = "Last name can't be empty")
    @Column(length = 80, nullable = false)
    @Length(min = 2, max = 80, message = "Last name can't be shorter than 2 and longer than 80 characters")
    private String lastName;

    @NotNull(message = "Age can't be empty")
    @Min(value = 10, message = "Your age can't be lower than 10 years old")
    @Max(value = 100, message = "Your age cant be greater than 100 years old")
    private Integer age;

    @NotNull(message = "Experience can't be empty")
    @Column(length = 10, nullable = false)
    @Min(value = 0,message = "Experience can't be lower than 0 years")
    @Max(value = 100,message = "Experience can't be lower than 100 years")
    private Integer experience;

    // dodawane w updateBio service
    @Column(length = 1000)
    @Length(min = 20, max = 1000, message = "Bio can't be shorter than 20 and longer than 1000 characters")
    private String bio;

    // tylko dla Coach
    // dodawane w addSpecialisation w service
    @Column(length = 80)
    @Length(min = 2, max = 80, message = "Specialisation can't be shorter than 2 and longer than 80 characters")
    private String specialisation;

    // to pole ustawiamy w serwisie
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
