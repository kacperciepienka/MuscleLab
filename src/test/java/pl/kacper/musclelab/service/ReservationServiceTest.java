package pl.kacper.musclelab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kacper.musclelab.mapper.create.CreateReservationMapper;
import pl.kacper.musclelab.mapper.entity.ReservationClientDTOMapper;
import pl.kacper.musclelab.mapper.entity.ReservationCoachDTOMapper;
import pl.kacper.musclelab.repository.ReservationRepository;
import pl.kacper.musclelab.repository.TrainingSlotRepository;
import pl.kacper.musclelab.repository.UserRepository;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrainingSlotRepository trainingSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreateReservationMapper createReservationMapper;
    @Mock

    private ReservationClientDTOMapper reservationClientDTOMapper;

    @Mock
    private ReservationCoachDTOMapper reservationCoachDTOMapper;

    @InjectMocks
    private ReservationService reservationService;

    @Nested
    @DisplayName("Testy dodania rezerwacji")
    class AddReservationTest{

        @Test
        @DisplayName("Make reservation as Client (Happy path)")
        void shouldMakeReservationClient(){






        }

        @Test
        @DisplayName("Make reservation as Coach (Happy path)")
        void shouldMakeReservationCoach(){






        }
    }
}
