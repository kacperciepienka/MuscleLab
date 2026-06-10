package pl.kacper.musclelab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 80, unique = true, nullable = false)
    @Length(min = 5, max = 80, message = "Reservation code can't be lesser than 5 and longer than 80 characters")
    private String reservationCode;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @OneToOne
    @JoinColumn(name = "training_slot_id", nullable = false)
    private TrainingSlot trainingSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @NotNull(message = "Created date can't be empty")
    private LocalDateTime createdAt;
}
