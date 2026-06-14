package pl.kacper.musclelab.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kacper.musclelab.dto.create.CreateTrainingSlot;
import pl.kacper.musclelab.dto.entity.TrainingSlotCoachDTO;
import pl.kacper.musclelab.exception.business.CantUpdateTrainingSlotTime;
import pl.kacper.musclelab.exception.business.WrongRoleException;
import pl.kacper.musclelab.exception.business.WrongSlotStatusException;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.validation.IncorrectUserException;
import pl.kacper.musclelab.exception.validation.TooBigDifferenceBetweenOriginalDateException;
import pl.kacper.musclelab.exception.validation.WrongTrainingDataException;
import pl.kacper.musclelab.mapper.create.CreateTrainingSlotMapper;
import pl.kacper.musclelab.mapper.entity.TrainingSlotClientDTOMapper;
import pl.kacper.musclelab.mapper.entity.TrainingSlotCoachDTOMapper;
import pl.kacper.musclelab.model.Role;
import pl.kacper.musclelab.model.SlotStatus;
import pl.kacper.musclelab.model.TrainingSlot;
import pl.kacper.musclelab.model.User;
import pl.kacper.musclelab.repository.TrainingSlotRepository;
import pl.kacper.musclelab.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class TrainingSlotServiceTest {

    @Mock
    private TrainingSlotRepository trainingSlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreateTrainingSlotMapper createTrainingSlotMapper;

    @Mock
    private TrainingSlotClientDTOMapper trainingSlotClientDTOMapper;

    @Mock
    private TrainingSlotCoachDTOMapper trainingSlotCoachDTOMapper;

    @InjectMocks
    private TrainingSlotService trainingSlotService;

    @Nested
    @DisplayName("Testy dodania slota")
    class AddTrainingSlotTest {

        @Test
        @DisplayName("Add training slot (Happy path)")
        void shouldAddTrainingSlot() {
            String coachUsername = "coach1";
            String coachEmail = "coach@wp.pl";
            String coachFirstname = "Piotr";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            User coach = User.builder()
                    .username(coachUsername)
                    .email(coachEmail)
                    .firstName(coachFirstname)
                    .role(Role.COACH)
                    .build();

            CreateTrainingSlot createTrainingSlot = CreateTrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            TrainingSlotCoachDTO trainingSlotCoachDTO = TrainingSlotCoachDTO.builder()
                    .coachUsername(coachUsername)
                    .coachFirstName(coachFirstname)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(coach.getUsername())).thenReturn(Optional.of(coach));
            when(createTrainingSlotMapper.toEntity(createTrainingSlot)).thenReturn(trainingSlot);
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(trainingSlotCoachDTOMapper.toDto(trainingSlot)).thenReturn(trainingSlotCoachDTO);

            TrainingSlotCoachDTO result = trainingSlotService.createTrainingSlot(createTrainingSlot, coachUsername);

            assertAll("test",
                    () -> assertEquals("coach1", result.getCoachUsername()),
                    () -> assertEquals("Piotr", result.getCoachFirstName()),
                    () -> assertEquals(startTime, result.getStartTime().truncatedTo(ChronoUnit.SECONDS)),
                    () -> assertEquals(endTime, result.getEndTime().truncatedTo(ChronoUnit.SECONDS)),
                    () -> assertEquals(SlotStatus.AVAILABLE, result.getStatus()),

                    () -> assertEquals(startTime, trainingSlot.getStartTime().truncatedTo(ChronoUnit.SECONDS)),
                    () -> assertEquals(endTime, trainingSlot.getEndTime().truncatedTo(ChronoUnit.SECONDS)),
                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.AVAILABLE, trainingSlot.getStatus()),

                    () -> assertNotNull(trainingSlot.getSlotCode()),
                    () -> assertTrue(trainingSlot.getSlotCode().startsWith("CO-"))
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(createTrainingSlotMapper, times(1)).toEntity(createTrainingSlot);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(trainingSlotCoachDTOMapper, times(1)).toDto(trainingSlot);
        }

        @Test
        @DisplayName("Add training slot (Fail path - coach does not exist)")
        void shouldThrowUserNotFoundException() {
            String coachUsername = "coachFake";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            CreateTrainingSlot createTrainingSlot = CreateTrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> trainingSlotService.createTrainingSlot(createTrainingSlot, coachUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(createTrainingSlotMapper, never()).toEntity(any(CreateTrainingSlot.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Add training slot (Fail path - Wrong role)")
        void shouldThrowWrongRoleException() {
            String coachUsername = "pablo";
            String coachEmail = "pablo@wp.pl";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);


            User coach = User.builder()
                    .username(coachUsername)
                    .email(coachEmail)
                    .role(Role.CLIENT)
                    .build();

            CreateTrainingSlot createTrainingSlot = CreateTrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(coach.getUsername())).thenReturn(Optional.of(coach));

            assertThrows(WrongRoleException.class,
                    () -> trainingSlotService.createTrainingSlot(createTrainingSlot, coach.getUsername()));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(createTrainingSlotMapper, never()).toEntity(any(CreateTrainingSlot.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Add training slot (Fail path - Wrong training data)")
        void shouldThrowWrongTrainingDataExceptionBeforeToday() {
            String coachUsername = "pablo";
            String coachEmail = "pablo@wp.pl";

            LocalDateTime startTime = LocalDateTime.now().minusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);


            User coach = User.builder()
                    .username(coachUsername)
                    .email(coachEmail)
                    .role(Role.COACH)
                    .build();

            CreateTrainingSlot createTrainingSlot = CreateTrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(coach.getUsername())).thenReturn(Optional.of(coach));

            assertThrows(WrongTrainingDataException.class,
                    () -> trainingSlotService.createTrainingSlot(createTrainingSlot, coach.getUsername()));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(createTrainingSlotMapper, never()).toEntity(any(CreateTrainingSlot.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Add training slot (Fail path - Wrong training data)")
        void shouldThrowWrongTrainingDataException() {
            String coachUsername = "pablo";
            String coachEmail = "pablo@wp.pl";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.minusHours(2).truncatedTo(ChronoUnit.SECONDS);

            User coach = User.builder()
                    .username(coachUsername)
                    .email(coachEmail)
                    .role(Role.COACH)
                    .build();

            CreateTrainingSlot createTrainingSlot = CreateTrainingSlot.builder()
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(coach.getUsername())).thenReturn(Optional.of(coach));

            assertThrows(WrongTrainingDataException.class,
                    () -> trainingSlotService.createTrainingSlot(createTrainingSlot, coach.getUsername()));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(createTrainingSlotMapper, never()).toEntity(any(CreateTrainingSlot.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }
    }

    @Nested
    @DisplayName("Testy anulowania slota")
    class CancelTrainingSlotTest {

        @Test
        @DisplayName("Cancel training slot (Happy path)")
        void shouldCancelTrainingSlot() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            TrainingSlotCoachDTO trainingSlotCoachDTO = TrainingSlotCoachDTO.builder()
                    .slotCode(slotCode)
                    .coachUsername(coachUsername)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.CANCELLED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(trainingSlotCoachDTOMapper.toDto(trainingSlot)).thenReturn(trainingSlotCoachDTO);

            TrainingSlotCoachDTO result = trainingSlotService.cancelSlot(slotCode, coachUsername);

            assertAll("test",
                    () -> assertEquals(slotCode, result.getSlotCode()),
                    () -> assertEquals(coachUsername, result.getCoachUsername()),
                    () -> assertEquals(startTime, result.getStartTime()),
                    () -> assertEquals(endTime, result.getEndTime()),
                    () -> assertEquals(SlotStatus.CANCELLED, result.getStatus()),

                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.CANCELLED, trainingSlot.getStatus())
            );

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(trainingSlotCoachDTOMapper, times(1)).toDto(trainingSlot);
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - training slot not found)")
        void shouldThrowTrainingSlotNotFoundException() {
            String coachUsername = "coach1";
            String slotCode = "CO-1229385782714";

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.empty());

            assertThrows(TrainingSlotNotFoundException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, never()).findUserByUsernameEqualsIgnoreCase(any(String.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - coach not found)")
        void shouldThrowUserNotFoundException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - wrong role)")
        void shouldThrowWrongRoleException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.CLIENT)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongRoleException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - incorrect coach)")
        void shouldThrowIncorrectUserException() {
            String coachUsername = "coach1";
            String fakeCoachUsername = "fakeCoach";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            User fakeCoach = User.builder()
                    .username(fakeCoachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(fakeCoachUsername)).thenReturn(Optional.of(fakeCoach));

            assertThrows(IncorrectUserException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, fakeCoachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(fakeCoachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - wrong training slot status)")
        void shouldThrowWrongSlotStatusExceptionStatusBooked() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - wrong training slot status)")
        void shouldThrowWrongSlotStatusExceptionStatusCompleted() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.COMPLETED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Cancel training slot (Failure path - wrong training slot status)")
        void shouldThrowWrongSlotStatusExceptionStatusCancelled() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.CANCELLED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> trainingSlotService.cancelSlot(slotCode, coachUsername));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }
    }


    @Nested
    @DisplayName("Zmiana czasu treningu")
    class UpdateTrainingTimeTest {

        @Test
        @DisplayName("Change slot time when slot is AVAILABLE (Happy path)")
        void shouldChangeTrainingTimeAvailable() {
            // basic data
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";


            // data for update
            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            TrainingSlotCoachDTO trainingSlotCoachDTO = TrainingSlotCoachDTO.builder()
                    .slotCode(slotCode)
                    .coachUsername(coachUsername)
                    .startTime(newStartTime)
                    .endTime(newEndTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(trainingSlotCoachDTOMapper.toDto(trainingSlot)).thenReturn(trainingSlotCoachDTO);

            TrainingSlotCoachDTO result = trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime);

            assertAll("test",
                    () -> assertEquals(slotCode, result.getSlotCode()),
                    () -> assertEquals(coachUsername, result.getCoachUsername()),
                    () -> assertEquals(newStartTime, result.getStartTime()),
                    () -> assertEquals(newEndTime, result.getEndTime()),
                    () -> assertEquals(SlotStatus.AVAILABLE, result.getStatus()),

                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(slotCode, trainingSlot.getSlotCode()),
                    () -> assertEquals(SlotStatus.AVAILABLE, trainingSlot.getStatus()),
                    () -> assertEquals(newStartTime, trainingSlot.getStartTime()),
                    () -> assertEquals(newEndTime, trainingSlot.getEndTime()),
                    () -> assertEquals(coachUsername, coach.getUsername())
            );

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(trainingSlotCoachDTOMapper, times(1)).toDto(trainingSlot);
        }

        @Test
        @DisplayName("Change slot time when slot is Booked (Happy path)")
        void shouldChangeTrainingTimeBooked() {
            // basic data
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-1229385782714";


            // data for update
            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .id(1L)
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            TrainingSlotCoachDTO trainingSlotCoachDTO = TrainingSlotCoachDTO.builder()
                    .slotCode(slotCode)
                    .coachUsername(coachUsername)
                    .startTime(newStartTime)
                    .endTime(newEndTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(trainingSlotCoachDTOMapper.toDto(trainingSlot)).thenReturn(trainingSlotCoachDTO);

            TrainingSlotCoachDTO result = trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime);

            assertAll("test",
                    () -> assertEquals(slotCode, result.getSlotCode()),
                    () -> assertEquals(coachUsername, result.getCoachUsername()),
                    () -> assertEquals(newStartTime, result.getStartTime()),
                    () -> assertEquals(newEndTime, result.getEndTime()),
                    () -> assertEquals(SlotStatus.BOOKED, result.getStatus()),

                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(slotCode, trainingSlot.getSlotCode()),
                    () -> assertEquals(SlotStatus.BOOKED, trainingSlot.getStatus()),
                    () -> assertEquals(newStartTime, trainingSlot.getStartTime()),
                    () -> assertEquals(newEndTime, trainingSlot.getEndTime()),
                    () -> assertEquals(coachUsername, coach.getUsername())
            );

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(trainingSlotCoachDTOMapper, times(1)).toDto(trainingSlot);
        }

        //Testy błędów
        @Test
        @DisplayName("Change slot time (Fail path - training slot does not exist)")
        void shouldThrowTrainingSlotNotFoundException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";
            String fakeSlotCode = "CO-1274839572994";

            // data for update
            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            // udajemy że nie ma takiego slota
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(fakeSlotCode)).thenReturn(Optional.empty());

            assertThrows(TrainingSlotNotFoundException.class,
                    () -> trainingSlotService.updateTrainingTime(fakeSlotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(fakeSlotCode);
            verify(userRepository, never()).findUserByUsernameEqualsIgnoreCase(any(String.class));
            verify(trainingSlotRepository, never()).save(trainingSlot);
            verify(trainingSlotCoachDTOMapper, never()).toDto(trainingSlot);
        }

        @Test
        @DisplayName("Change slot time (Fail path - user (coach) does not exist)")
        void shouldThrowUserNotFoundException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();


            // udajemy że ten trener nie istnieje
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong role)")
        void shouldThrowWrongRoleException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.CLIENT)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongRoleException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong Coach)")
        void shouldThrowIncorrectUserException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            String fakeCoachUsername = "fake coach";

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            User fakeCoach = User.builder()
                    .username(fakeCoachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(fakeCoachUsername)).thenReturn(Optional.of(fakeCoach));

            assertThrows(IncorrectUserException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, fakeCoachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(fakeCoachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong new start time)")
        void shouldThrowWrongTrainingDataExceptionStartTime() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.minusDays(11);
            LocalDateTime newEndTime = newStartTime.plusHours(2);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongTrainingDataException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong new end time)")
        void shouldThrowWrongTrainingDataExceptionEndTime() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.minusHours(3);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongTrainingDataException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong slot status - COMPLETED)")
        void shouldThrowWrongSlotStatusExceptionCompleted() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.COMPLETED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - wrong slot status - CANCELLED)")
        void shouldThrowWrongSlotStatusExceptionCancelled() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusHours(1);
            LocalDateTime newEndTime = endTime.plusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.CANCELLED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time when slot is BOOKED (Fail path - too late for making changes)")
        void shouldThrowCantUpdateTrainingSlotTime() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusHours(7).plusMinutes(59).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.minusHours(1);
            LocalDateTime newEndTime = endTime.minusHours(1);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(CantUpdateTrainingSlotTime.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }

        @Test
        @DisplayName("Change slot time (Fail path - too big difference between original start/end time)")
        void shouldThrowTooBigDifferenceBetweenOriginalDateException() {
            String coachUsername = "coach1";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String slotCode = "CO-172847268419";

            LocalDateTime newStartTime = startTime.plusMinutes(121);
            LocalDateTime newEndTime = endTime.plusMinutes(120);

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)).thenReturn(Optional.of(trainingSlot));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(TooBigDifferenceBetweenOriginalDateException.class,
                    () -> trainingSlotService.updateTrainingTime(slotCode, coachUsername, newStartTime, newEndTime));

            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(trainingSlotCoachDTOMapper, never()).toDto(any(TrainingSlot.class));
        }
    }
}
