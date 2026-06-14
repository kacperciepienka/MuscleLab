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
import pl.kacper.musclelab.exception.business.TooManyTrainingThisWeekException;
import pl.kacper.musclelab.exception.business.TooManyTrainingTodayException;
import pl.kacper.musclelab.exception.business.WrongRoleException;
import pl.kacper.musclelab.exception.business.WrongSlotStatusException;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
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
    class AddReservationTest{

        @Test
        @DisplayName("Make reservation as Client (Happy path)")
        void shouldMakeReservationClient(){
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
                    () -> assertEquals(coachUsername, result.getReservationTrainingSlotCoachUsername()) ,
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
        void shouldThrowUserNotFoundException(){
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
        void shouldThrowTrainingSlotNotFoundException(){
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
        void shouldThrowWrongRoleException(){
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

            Reservation reservation = Reservation.builder()
                    .reservationCode(reservationCode)
                    .client(client)
                    .trainingSlot(trainingSlot)
                    .createdAt(createdAt)
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
        void shouldThrowWrongSlotStatusExceptionBooked(){
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
                    .status(SlotStatus.BOOKED)
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
        void shouldThrowWrongSlotStatusExceptionCancelled(){
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
                    .status(SlotStatus.CANCELLED)
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
        void shouldThrowWrongSlotStatusExceptionCompleted(){
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
                    .status(SlotStatus.COMPLETED)
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
        void shouldThrowWrongTrainingDataException(){
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
        void shouldThrowTooManyTrainingTodayException(){
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
        void shouldThrowTooManyTrainingThisWeekException(){
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
}
