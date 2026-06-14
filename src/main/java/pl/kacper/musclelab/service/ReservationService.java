package pl.kacper.musclelab.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kacper.musclelab.dto.create.CreateReservation;
import pl.kacper.musclelab.dto.entity.ReservationClientDTO;
import pl.kacper.musclelab.dto.entity.ReservationCoachDTO;
import pl.kacper.musclelab.dto.filter.ReservationClientFilter;
import pl.kacper.musclelab.dto.filter.ReservationCoachFilter;
import pl.kacper.musclelab.exception.not_Found.ReservationNotFoundException;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.business.*;
import pl.kacper.musclelab.exception.validation.IncorrectUserException;
import pl.kacper.musclelab.exception.validation.WrongTrainingDataException;
import pl.kacper.musclelab.mapper.create.CreateReservationMapper;
import pl.kacper.musclelab.mapper.entity.ReservationClientDTOMapper;
import pl.kacper.musclelab.mapper.entity.ReservationCoachDTOMapper;
import pl.kacper.musclelab.model.*;
import pl.kacper.musclelab.repository.ReservationRepository;
import pl.kacper.musclelab.repository.TrainingSlotRepository;
import pl.kacper.musclelab.repository.UserRepository;
import pl.kacper.musclelab.specification.ReservationClientSpecification;
import pl.kacper.musclelab.specification.ReservationCoachSpecification;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;    // <-- repozytorium
    private final TrainingSlotRepository trainingSlotRepository;  // <-- repozytorium
    private final UserRepository userRepository;                  // <-- repozytorium

    private final CreateReservationMapper createReservationMapper; // <-- tworzenie rezerwacji

    private final ReservationClientDTOMapper reservationClientDTOMapper;  // widok
    private final ReservationCoachDTOMapper reservationCoachDTOMapper;    // widok

    Random random = new Random();

    // Make reservation (tylko client może robić nową rezerwację)
    public ReservationClientDTO makeReservation(CreateReservation createReservation, String clientUsername) {
        User client = userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)
                .orElseThrow(() -> new UserNotFoundException(clientUsername));

        TrainingSlot slot = trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(createReservation.getTrainingSlotCode())
                .orElseThrow(() -> new TrainingSlotNotFoundException(createReservation.getTrainingSlotCode()));

        if (client.getRole() != Role.CLIENT) {
            throw new WrongRoleException();
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new WrongSlotStatusException(slot.getSlotCode());
        }

        Reservation reservation = createReservationMapper.toEntity(createReservation);

        // automat do sprawdzania treningów, bo jeśli minęła godzina startu
        //a slot jest dalej Available to musi się zmienić na nowy status MISSED

        LocalDateTime createdAt = LocalDateTime.now();

        if (slot.getStartTime().isBefore(createdAt)) {
            throw new WrongTrainingDataException();
        }

        LocalDateTime dayStart = slot.getStartTime().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        long reservationsThisDay =
                reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeBetween(
                        client,
                        ReservationStatus.BOOKED,
                        dayStart,
                        dayEnd
                );

        if (reservationsThisDay > 3) {
            throw new TooManyTrainingTodayException();
        }

        long futureReservations =
                reservationRepository.countByClientAndStatusAndTrainingSlot_StartTimeAfter(
                        client,
                        ReservationStatus.BOOKED,
                        LocalDateTime.now()
                );

        if (futureReservations > 10) {
            throw new TooManyTrainingThisWeekException();
        }

        int day = createdAt.getDayOfMonth();
        int randomNumber = random.nextInt(10000000, 99999999);
        String prefix1 = client.getUsername().toUpperCase().substring(0, 2);
        String prefix2 = slot.getCoach().getUsername().toUpperCase().substring(0, 2);
        String reservationCode = prefix1 + "-" + day + randomNumber + "-" + prefix2;

        reservation.setReservationCode(reservationCode);
        reservation.setClient(client);
        reservation.setTrainingSlot(slot);
        reservation.setStatus(ReservationStatus.BOOKED);
        reservation.setCreatedAt(createdAt);

        slot.setStatus(SlotStatus.BOOKED);
        trainingSlotRepository.save(slot);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationClientDTOMapper.toDto(savedReservation);
    }

    //PUT
    // logika: Client zmienia na canceled slot -> AVAILABLE
    // Coach zmienia na canceled -> slot Canceled
    // tylko Coach może zmieniać na COMPLETED

    public ReservationClientDTO cancelReservationAsClient(String reservationCode, String clientUsername) {
        Reservation reservation = reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)
                .orElseThrow(() -> new ReservationNotFoundException(reservationCode));

        User client = userRepository.findUserByUsernameEqualsIgnoreCase(clientUsername)
                .orElseThrow(() -> new UserNotFoundException(clientUsername));

        if (!client.getUsername().equalsIgnoreCase(reservation.getClient().getUsername())){
            throw new IncorrectUserException(client.getUsername());
        }

        // gdy rezerwacja jest zakończona albo odwołana nie można jej zmieniać
        if (reservation.getStatus() != ReservationStatus.BOOKED){
            throw new WrongReservationStatus(reservation.getReservationCode());
        }

        if (client.getRole() != Role.CLIENT) {
            throw new WrongRoleException();
        }

        if (reservation.getTrainingSlot().getStartTime().isBefore(LocalDateTime.now().plusHours(12))) {
            throw new CantCancelTrainingException();
        }

        reservation.getTrainingSlot().setStatus(SlotStatus.AVAILABLE);
        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationClientDTOMapper.toDto(savedReservation);
    }

    public ReservationCoachDTO cancelReservationAsCoach(String reservationCode, String coachUsername) {
        Reservation reservation = reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)
                .orElseThrow(() -> new ReservationNotFoundException(reservationCode));

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (!coach.getUsername().equalsIgnoreCase(reservation.getTrainingSlot().getCoach().getUsername())){
            throw new IncorrectUserException(coach.getUsername());
        }

        if (reservation.getStatus() != ReservationStatus.BOOKED){
            throw new WrongReservationStatus(reservation.getReservationCode());
        }

        if (coach.getRole() != Role.COACH) {
            throw new WrongRoleException();
        }

        if (reservation.getTrainingSlot().getStartTime().isBefore(LocalDateTime.now().plusHours(12))) {
            throw new CantCancelTrainingException();
        }

        reservation.getTrainingSlot().setStatus(SlotStatus.CANCELLED);
        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationCoachDTOMapper.toDto(savedReservation);
    }

    public ReservationCoachDTO completeTrainingAsACoach(String reservationCode, String coachUsername) {
        Reservation reservation = reservationRepository.findReservationByReservationCodeEqualsIgnoreCase(reservationCode)
                .orElseThrow(() -> new ReservationNotFoundException(reservationCode));

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (!coach.getUsername().equalsIgnoreCase(reservation.getTrainingSlot().getCoach().getUsername())){
            throw new IncorrectUserException(coach.getUsername());
        }

        if (reservation.getStatus() != ReservationStatus.BOOKED){
            throw new WrongReservationStatus(reservation.getReservationCode());
        }

        if (coach.getRole() != Role.COACH) {
            throw new WrongRoleException();
        }

        if (reservation.getTrainingSlot().getEndTime().isAfter(LocalDateTime.now())) {
            throw new CantCompleteTrainingBeforeItEndsException();
        }

        reservation.getTrainingSlot().setStatus(SlotStatus.COMPLETED);
        reservation.setStatus(ReservationStatus.COMPLETED);

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationCoachDTOMapper.toDto(savedReservation);
    }


    // widoki prywatne dla COACH i CLIENT
    public Page<ReservationClientDTO> showAllReservationForClient(
            String username,
            ReservationClientFilter filter,
            Pageable pageable) {

        User user = userRepository.findUserByUsernameEqualsIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (user.getRole() != Role.CLIENT){
            throw new WrongRoleException();
        }

        return reservationRepository
                .findAll(ReservationClientSpecification.filter(username, filter), pageable)
                .map(reservationClientDTOMapper::toDto);
    }

    public Page<ReservationCoachDTO> showAllReservationForCoach(
            String username,
            ReservationCoachFilter filter,
            Pageable pageable){

        User user = userRepository.findUserByUsernameEqualsIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (user.getRole() != Role.COACH){
            throw new WrongRoleException();
        }

        return reservationRepository
                .findAll(ReservationCoachSpecification.filter(username, filter), pageable)
                .map(reservationCoachDTOMapper::toDto);
    }
}
