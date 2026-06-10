package pl.kacper.musclelab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // kod identyfikacyjny prosty do stworzenia rezerwacji kod typu KA-05072026-1730
    @Column(length = 80, unique = true, nullable = false)
    @Length(min = 5, max = 80, message = "Slot code can't be lesser than 5 and longer than 80 characters")
    private String slotCode;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    // będzie dopisany w locie w service
    private User coach;

    @NotNull(message = "Start time can't be empty")
    private LocalDateTime startTime;

    @NotNull(message = "End time can't be empty")
    private LocalDateTime endTime;

    // w service ustawienie na AVAILABLE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;
}
