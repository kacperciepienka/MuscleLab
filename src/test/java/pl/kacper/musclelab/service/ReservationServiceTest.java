package pl.kacper.musclelab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.kacper.musclelab.dto.create.CreateReservation;
import pl.kacper.musclelab.dto.entity.ReservationClientDTO;
import pl.kacper.musclelab.dto.entity.ReservationCoachDTO;
import pl.kacper.musclelab.exception.business.*;
import pl.kacper.musclelab.exception.not_Found.ReservationNotFoundException;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.validation.IncorrectUserException;
import pl.kacper.musclelab.exception.validation.WrongTrainingDataException;
import pl.kacper.musclelab.mapper.create.CreateReservationMapper;
import pl.kacper.musclelab.mapper.entity.ReservationClientDTOMapper;
import pl.kacper.musclelab.mapper.entity.ReservationCoachDTOMapper;
import pl.kacper.musclelab.model.*;
import pl.kacper.musclelab.repository.ReservationRepository;
import pl.kacper.musclelab.repository.TrainingSlotRepository;
import pl.kacper.musclelab.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
    class AddReservationTest {

        @Test
        @DisplayName("Make reservation as Client (Happy path)")
        void shouldMakeReservationClient() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            String reservationCode = "CO-1247828942CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .build();

            ReservationClientDTO reservationClientDTO = ReservationClientDTO.builder()
                    .reservationClientUsername(clientUsername)
                    .reservationTrainingSlotCode(trainingSlotCode)
                    .reservationTrainingSlotCoachUsername(coachUsername)
                    .reservationTrainingSlotStartTime(startTime)
                    .reservationTrainingSlotEndTime(endTime)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));
            when(createReservationMapper.toEntity(createReservation)).thenReturn(reservation);
            when(reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd)).thenReturn(0L);
            when(reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class))).thenReturn(0L);
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationClientDTOMapper.toDto(reservation)).thenReturn(reservationClientDTO);

            ReservationClientDTO result = reservationService.makeReservation(createReservation, clientUsername);

            assertAll("test",
                    () -> assertEquals(clientUsername, result.getReservationClientUsername()),
                    () -> assertEquals(trainingSlotCode, result.getReservationTrainingSlotCode()),
                    () -> assertEquals(coachUsername, result.getReservationTrainingSlotCoachUsername()),
                    () -> assertEquals(startTime, result.getReservationTrainingSlotStartTime()),
                    () -> assertEquals(endTime, result.getReservationTrainingSlotEndTime()),
                    () -> assertEquals(ReservationStatus.BOOKED, result.getStatus()),

                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.BOOKED, trainingSlot.getStatus()),

                    () -> assertEquals(coachUsername, coach.getUsername()),
                    () -> assertEquals(clientUsername, client.getUsername())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, times(1)).toEntity(createReservation);
            verify(reservationRepository, times(1)).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, times(1)).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(reservationRepository, times(1)).save(reservation);
            verify(reservationClientDTOMapper, times(1)).toDto(reservation);
        }

        @Test
        @DisplayName("Make reservation (Fail path - client does not exist)")
        void shouldThrowUserNotFoundException() {
            String clientUsername = "client1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).findTrainingSlotBySlotCodeEqualsIgnoreCase(any(String.class));
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - training slot does not exist)")
        void shouldThrowTrainingSlotNotFoundException() {
            String clientUsername = "client1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.empty());

            assertThrows(TrainingSlotNotFoundException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - client has wrong role)")
        void shouldThrowWrongRoleException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.COACH)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));

            assertThrows(WrongRoleException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - training slot has wrong status (BOOKED))")
        void shouldThrowWrongSlotStatusExceptionBooked() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - training slot has wrong status (CANCELLED))")
        void shouldThrowWrongSlotStatusExceptionCancelled() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.CANCELLED)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - training slot has wrong status (Completed))")
        void shouldThrowWrongSlotStatusExceptionCompleted() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.COMPLETED)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, never()).toEntity(any(CreateReservation.class));
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - reservation created at wrong)")
        void shouldThrowWrongTrainingDataException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().minusMinutes(1).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            String reservationCode = "CO-1247828942CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));
            when(createReservationMapper.toEntity(createReservation)).thenReturn(reservation);

            assertThrows(WrongTrainingDataException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, times(1)).toEntity(createReservation);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - too many training Today (max. 3))")
        void shouldThrowTooManyTrainingTodayException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            String reservationCode = "CO-1247828942CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));
            when(createReservationMapper.toEntity(createReservation)).thenReturn(reservation);
            when(reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd)).thenReturn(4L);

            assertThrows(TooManyTrainingTodayException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, times(1)).toEntity(createReservation);
            verify(reservationRepository, times(1)).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, never()).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Make reservation (Fail path - too many training this Week (max. 10))")
        void shouldThrowTooManyTrainingThisWeekException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);
            String trainingSlotCode = "CO-124782894214";

            String reservationCode = "CO-1247828942CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay().truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime dayEnd = dayStart.plusDays(1).truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.COACH)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(trainingSlotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            CreateReservation createReservation = CreateReservation.builder()
                    .trainingSlotCode(trainingSlotCode)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode)).thenReturn(Optional.of(trainingSlot));
            when(createReservationMapper.toEntity(createReservation)).thenReturn(reservation);
            when(reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd)).thenReturn(3L);
            when(reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class))).thenReturn(11L);


            assertThrows(TooManyTrainingThisWeekException.class,
                    () -> reservationService.makeReservation(createReservation, clientUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).findTrainingSlotBySlotCodeEqualsIgnoreCase(trainingSlotCode);
            verify(createReservationMapper, times(1)).toEntity(createReservation);
            verify(reservationRepository, times(1)).countByClientAndStatusAndTrainingSlot_StartTimeBetween(client, ReservationStatus.BOOKED, dayStart, dayEnd);
            verify(reservationRepository, times(1)).countByClientAndStatusAndTrainingSlot_StartTimeAfter(eq(client), eq(ReservationStatus.BOOKED), any(LocalDateTime.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Testy anulowania rezerwacji jako klient")
    class CancelReservationAsClientTest {

        @Test
        @DisplayName("Cancel reservation as Client (Happy path)")
        void shouldCancelReservation() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED) // przed jest BOOKED
                    .createdAt(createdAt)
                    .build();

            ReservationClientDTO reservationClientDTO = ReservationClientDTO.builder()
                    .reservationCode(reservationCode)
                    .reservationClientUsername(clientUsername)
                    .reservationTrainingSlotCode(slotCode)
                    .reservationTrainingSlotCoachUsername(coachUsername)
                    .reservationTrainingSlotStartTime(startTime)
                    .reservationTrainingSlotEndTime(endTime)

                    .status(ReservationStatus.CANCELLED)  // już gotowe
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationClientDTOMapper.toDto(reservation)).thenReturn(reservationClientDTO);

            ReservationClientDTO result = reservationService.cancelReservationAsClient(reservationCode, clientUsername);

            assertAll("test",
                    () -> assertEquals(reservationCode, result.getReservationCode()),
                    () -> assertEquals(clientUsername, result.getReservationClientUsername()),
                    () -> assertEquals(slotCode, result.getReservationTrainingSlotCode()),
                    () -> assertEquals(coachUsername, result.getReservationTrainingSlotCoachUsername()),
                    () -> assertEquals(ReservationStatus.CANCELLED, result.getStatus())

                    ,
                    () -> assertEquals(reservationCode, reservation.getReservationCode()),
                    () -> assertEquals(client, reservation.getClient()),
                    () -> assertEquals(trainingSlot, reservation.getTrainingSlot()),
                    () -> assertEquals(ReservationStatus.CANCELLED, reservation.getStatus()),

                    () -> assertEquals(slotCode, trainingSlot.getSlotCode()),
                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.AVAILABLE, trainingSlot.getStatus()),

                    () -> assertEquals(coachUsername, coach.getUsername()),
                    () -> assertEquals(clientUsername, client.getUsername())
            );

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(reservationRepository, times(1)).save(reservation);
            verify(reservationClientDTOMapper, times(1)).toDto(reservation);
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - reservation not found)")
        void shouldThrowReservationNotFoundException() {
            String clientUsername = "client1";
            String reservationCode = "CO-3847396719-CO";

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.empty());

            assertThrows(ReservationNotFoundException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, never()).findUserByUsernameEqualsIgnoreCase(any(String.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - client not found)")
        void shouldThrowUserNotFoundException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }


        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong role)")
        void shouldThrowWrongRoleException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.COACH)  // <- wrong role
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongRoleException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - incorrect user)")
            // nie możesz usunąć cudzej rezerwacji
        void shouldThrowIncorrectUserException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";
            String fakeUserUsername = "fakeClient";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User fakeClient = User.builder()
                    .username(fakeUserUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(fakeUserUsername)).thenReturn(Optional.of(fakeClient));

            assertThrows(IncorrectUserException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, fakeUserUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(fakeUserUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong reservation status (COMPLETED))")
        void shouldThrowWrongReservationStatusCompleted() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.COMPLETED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong reservation status (CANCELLED))")
        void shouldThrowWrongReservationStatusCancelled() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.CANCELLED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong training slot status (AVAILABLE))")
        void shouldThrowWrongSlotStatusExceptionAvailable() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong training slot status (COMPLETED))")
        void shouldThrowWrongSlotStatusExceptionCompleted() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - wrong training slot status (CANCELLED))")
        void shouldThrowWrongSlotStatusExceptionCancelled() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Client (Fail path - too close to training to cancel reservation (12h max.))")
        void shouldThrowCantCancelTrainingException() {
            String clientUsername = "client1";
            String coachUsername = "coach1";

            String slotCode = "CO-2328491749301";
            LocalDateTime startTime = LocalDateTime.now().plusHours(11).plusMinutes(59).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-3847396719-CO";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .status(ReservationStatus.BOOKED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)).thenReturn(Optional.of(client));

            assertThrows(CantCancelTrainingException.class,
                    () -> reservationService.cancelReservationAsClient(reservationCode, clientUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(clientUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationClientDTOMapper, never()).toDto(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Testy anulowaina rezerwacji jako trener")
    class CancelReservationAsCoachTest {

        @Test
        @DisplayName("Cancel reservation as Coach (Happy path)")
        void shouldCancelReservation() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            ReservationCoachDTO reservationCoachDTO = ReservationCoachDTO.builder()
                    .reservationCode(reservationCode)
                    .reservationClientUsername(clientUsername)
                    .reservationTrainingSlotCode(slotCode)
                    .reservationTrainingSlotCoachUsername(coachUsername)
                    .reservationTrainingSlotStartTime(startTime)
                    .reservationTrainingSlotEndTime(endTime)

                    .status(ReservationStatus.CANCELLED)
                    .createdAt(createdAt)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationCoachDTOMapper.toDto(reservation)).thenReturn(reservationCoachDTO);

            ReservationCoachDTO result = reservationService.cancelReservationAsCoach(reservationCode, coachUsername);

            assertAll("test",
                    () -> assertEquals(reservationCode, result.getReservationCode()),
                    () -> assertEquals(clientUsername, result.getReservationClientUsername()),
                    () -> assertEquals(slotCode, result.getReservationTrainingSlotCode()),
                    () -> assertEquals(coachUsername, result.getReservationTrainingSlotCoachUsername()),
                    () -> assertEquals(ReservationStatus.CANCELLED, result.getStatus()),

                    () -> assertEquals(reservationCode, reservation.getReservationCode()),
                    () -> assertEquals(client, reservation.getClient()),
                    () -> assertEquals(trainingSlot, reservation.getTrainingSlot()),
                    () -> assertEquals(ReservationStatus.CANCELLED, reservation.getStatus()),


                    () -> assertEquals(slotCode, trainingSlot.getSlotCode()),
                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.CANCELLED, trainingSlot.getStatus()),

                    () -> assertEquals(coachUsername, coach.getUsername()),
                    () -> assertEquals(clientUsername, client.getUsername())
            );

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(reservationRepository, times(1)).save(reservation);
            verify(reservationCoachDTOMapper, times(1)).toDto(reservation);
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - reservation not found)")
        void shouldThrowReservationNotFoundException() {
            String coachUsername = "coach1";
            String reservationCode = "CO-88374628931-CL";

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.empty());

            assertThrows(ReservationNotFoundException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, never()).findUserByUsernameEqualsIgnoreCase(any(String.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - coach not found)")
        void shouldThrowUserNotFoundException() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong role)")
        void shouldThrowWrongRoleException() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.CLIENT)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongRoleException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong coach)")
        void shouldThrowIncorrectUserException() {
            String coachUsername = "coach1";
            String fakeCoachUsername = "fakeCoach";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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
                    .status(SlotStatus.BOOKED)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(fakeCoachUsername)).thenReturn(Optional.of(fakeCoach));

            assertThrows(IncorrectUserException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, fakeCoachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(fakeCoachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong reservation status (CANCELLED))")
        void shouldThrowWrongReservationStatusCancelled() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.CANCELLED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong reservation status (COMPLETED))")
        void shouldThrowWrongReservationStatusCompleted() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.COMPLETED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong training slot status (AVAILABLE))")
        void shouldThrowWrongSlotStatusExceptionAvailable() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong training slot status (CANCELLED))")
        void shouldThrowWrongSlotStatusExceptionCancelled() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - wrong training slot status (COMPLETED))")
        void shouldThrowWrongSlotStatusExceptionCompleted() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Cancel reservation as Coach (Fail path - too close to training to cancel reservation (12h max.))")
        void shouldThrowCantCancelTrainingException() {
            String coachUsername = "coach1";
            String clientUsername = "client";

            String slotCode = "CO-1235290747714";
            LocalDateTime startTime = LocalDateTime.now().plusHours(11).plusMinutes(59).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-88374628931-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(CantCancelTrainingException.class,
                    () -> reservationService.cancelReservationAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Testy zakończenia rezerwacji jako trener")
    class CompleteReservationAsCoachTest {

        @Test
        @DisplayName("Complete reservation as Coach (Happy path)")
        void shouldCompleteReservation() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);  // ends 3 hours ago

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            ReservationCoachDTO reservationCoachDTO = ReservationCoachDTO.builder()
                    .reservationCode(reservationCode)
                    .reservationClientUsername(clientUsername)

                    .reservationTrainingSlotCode(slotCode)
                    .reservationTrainingSlotCoachUsername(coachUsername)
                    .reservationTrainingSlotStartTime(startTime)
                    .reservationTrainingSlotEndTime(endTime)

                    .createdAt(createdAt)
                    .status(ReservationStatus.COMPLETED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));
            when(trainingSlotRepository.save(trainingSlot)).thenReturn(trainingSlot);
            when(reservationRepository.save(reservation)).thenReturn(reservation);
            when(reservationCoachDTOMapper.toDto(reservation)).thenReturn(reservationCoachDTO);

            ReservationCoachDTO result = reservationService.completeTrainingAsCoach(reservationCode, coachUsername);

            assertAll("test",
                    () -> assertEquals(reservationCode, result.getReservationCode()),
                    () -> assertEquals(clientUsername, result.getReservationClientUsername()),
                    () -> assertEquals(slotCode, result.getReservationTrainingSlotCode()),
                    () -> assertEquals(coachUsername, result.getReservationTrainingSlotCoachUsername()),
                    () -> assertEquals(ReservationStatus.COMPLETED, result.getStatus()),

                    () -> assertEquals(reservationCode, reservation.getReservationCode()),
                    () -> assertEquals(client, reservation.getClient()),
                    () -> assertEquals(trainingSlot, reservation.getTrainingSlot()),
                    () -> assertEquals(ReservationStatus.COMPLETED, reservation.getStatus()),

                    () -> assertEquals(slotCode, trainingSlot.getSlotCode()),
                    () -> assertEquals(coach, trainingSlot.getCoach()),
                    () -> assertEquals(SlotStatus.COMPLETED, trainingSlot.getStatus()),

                    () -> assertEquals(coachUsername, coach.getUsername()),
                    () -> assertEquals(clientUsername, client.getUsername())
            );

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, times(1)).save(trainingSlot);
            verify(reservationRepository, times(1)).save(reservation);
            verify(reservationCoachDTOMapper, times(1)).toDto(reservation);
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - reservation not found)")
        void shouldThrowReservationNotFoundException() {
            String coachUsername = "coach1";
            String reservationCode = "CO-128847364932-CL";

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.empty());

            assertThrows(ReservationNotFoundException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, never()).findUserByUsernameEqualsIgnoreCase(any(String.class));
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - coach not found)")
        void shouldThrowUserNotFoundException() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong coach)")
        void shouldThrowIncorrectUserException() {
            String clientUsername = "client";
            String coachUsername = "coach1";
            String fakeCoachUsername = "fakeCoach";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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
                    .status(SlotStatus.BOOKED)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(fakeCoachUsername)).thenReturn(Optional.of(fakeCoach));

            assertThrows(IncorrectUserException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, fakeCoachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(fakeCoachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong reservation status COMPLETED)")
        void shouldThrowWrongReservationStatusCompleted() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.COMPLETED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong reservation status CANCELLED)")
        void shouldThrowWrongReservationStatusCancelled() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.CANCELLED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongReservationStatus.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong slot status status AVAILABLE)")
        void shouldThrowWrongSlotStatusAvailable() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong slot status status COMPLETED)")
        void shouldThrowWrongSlotStatusCompleted() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong slot status status CANCELLED)")
        void shouldThrowWrongSlotStatusCancelled() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongSlotStatusException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - wrong role)")
        void shouldThrowWrongRoleException() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(5).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

            User coach = User.builder()
                    .username(coachUsername)
                    .role(Role.CLIENT)
                    .build();

            TrainingSlot trainingSlot = TrainingSlot.builder()
                    .slotCode(slotCode)
                    .coach(coach)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(SlotStatus.BOOKED)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(WrongRoleException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }

        @Test
        @DisplayName("Complete reservation as Coach (Fail path - can not complete training before it ends)")
        void shouldThrowCantCompleteTrainingBeforeItEndsException() {
            String clientUsername = "client";
            String coachUsername = "coach1";

            String slotCode = "CO-1238476637214";
            LocalDateTime startTime = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime endTime = startTime.plusHours(2).truncatedTo(ChronoUnit.SECONDS);  // ends in 1 hour

            String reservationCode = "CO-128847364932-CL";
            LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            User client = User.builder()
                    .username(clientUsername)
                    .role(Role.CLIENT)
                    .build();

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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
                    .status(ReservationStatus.BOOKED)
                    .build();

            when(reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)).thenReturn(Optional.of(reservation));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)).thenReturn(Optional.of(coach));

            assertThrows(CantCompleteTrainingBeforeItEndsException.class,
                    () -> reservationService.completeTrainingAsCoach(reservationCode, coachUsername));

            verify(reservationRepository, times(1)).findReservationByReservationCodeEqualsIgnoreCase(reservationCode);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(coachUsername);
            verify(trainingSlotRepository, never()).save(any(TrainingSlot.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(reservationCoachDTOMapper, never()).toDto(any(Reservation.class));
        }
    }
}
