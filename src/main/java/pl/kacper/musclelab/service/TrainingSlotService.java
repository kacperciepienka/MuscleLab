package pl.kacper.musclelab.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kacper.musclelab.dto.create.CreateTrainingSlot;
import pl.kacper.musclelab.dto.entity.TrainingSlotClientDTO;
import pl.kacper.musclelab.dto.entity.TrainingSlotCoachDTO;
import pl.kacper.musclelab.dto.filter.TrainingSlotCoachFilter;
import pl.kacper.musclelab.dto.filter.TrainingSlotUserFilter;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.business.CantCancelTrainingException;
import pl.kacper.musclelab.exception.business.WrongRoleException;
import pl.kacper.musclelab.exception.business.WrongSlotStatusException;
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
import pl.kacper.musclelab.specification.TrainingSlotCoachSpecification;
import pl.kacper.musclelab.specification.TrainingSlotUserSpecification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainingSlotService {

    private final TrainingSlotRepository trainingSlotRepository; // <-- repozytorium
    private final UserRepository userRepository; // <-- repozytorium

    private final CreateTrainingSlotMapper createTrainingSlotMapper; // <-- tworzenie slota

    private final TrainingSlotClientDTOMapper trainingSlotClientDTOMapper; // <-- wygląd dla klienta
    private final TrainingSlotCoachDTOMapper trainingSlotCoachDTOMapper; // <-- wygląd dla trenera
    // Używamy tylko slotów, żeby je przeglądać. Tylko trener może zobaczyć swoje sloty nie jako rezerwację

    Random random = new Random();

    //POST add Slot
    public TrainingSlotCoachDTO createTrainingSlot(CreateTrainingSlot newTrainingSlot, String coachUsername) {
        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (coach.getRole() != Role.COACH) {
            throw new WrongRoleException();
        }

        if (newTrainingSlot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new WrongTrainingDataException();
        }

        if (!newTrainingSlot.getEndTime().isAfter(newTrainingSlot.getStartTime())) {
            throw new WrongTrainingDataException();
        }

        String prefix = coach.getUsername().toUpperCase().substring(0, 2);
        int hourStart = newTrainingSlot.getStartTime().getHour();
        int randomNum = random.nextInt(1000000, 9999999);
        int hourEnd = newTrainingSlot.getEndTime().getHour();
        String slotCode = prefix  + "-" + hourStart + randomNum + hourEnd;


        TrainingSlot slot = createTrainingSlotMapper.toEntity(newTrainingSlot);
        slot.setSlotCode(slotCode);
        slot.setCoach(coach);
        slot.setStatus(SlotStatus.AVAILABLE);

        TrainingSlot savedSlot = trainingSlotRepository.save(slot);
        return trainingSlotCoachDTOMapper.toDto(savedSlot);
    }

    // DELETE-nie istnieje możliwości usunięcia
    // PUT
    // zmień status na canceled (gdy slot jest niezarezerwowany)
    // nie robię Completed, bo to w reservation można zrobić
    public TrainingSlotCoachDTO cancelSlot(String slotCode,
                                           String coachUsername) {
        TrainingSlot slot = trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)
                .orElseThrow(() -> new TrainingSlotNotFoundException(slotCode));

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (coach.getRole() != Role.COACH){
            throw new WrongRoleException();
        }

        if (!slot.getCoach().getUsername().equalsIgnoreCase(coachUsername)){
            throw new IncorrectUserException(coachUsername);
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE){
            throw new WrongSlotStatusException(slotCode);
        }

        slot.setStatus(SlotStatus.CANCELLED);
        TrainingSlot changedSlot = trainingSlotRepository.save(slot);
        return trainingSlotCoachDTOMapper.toDto(changedSlot);
    }

    public TrainingSlotCoachDTO updateTrainingTime(String slotCode,
                                                   String coachUsername,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        TrainingSlot slot = trainingSlotRepository.findTrainingSlotBySlotCodeEqualsIgnoreCase(slotCode)
                .orElseThrow(() -> new TrainingSlotNotFoundException(slotCode));

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (coach.getRole() != Role.COACH){
            throw new WrongRoleException();
        }

        if (!slot.getCoach().getUsername().equalsIgnoreCase(coachUsername)){
            throw new IncorrectUserException(coachUsername);
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new WrongTrainingDataException();
        }

        if (!endTime.isAfter(startTime)) {
            throw new WrongTrainingDataException();
        }

        if (slot.getStatus() != SlotStatus.AVAILABLE && slot.getStatus() != SlotStatus.BOOKED) {
            throw new WrongSlotStatusException(slotCode);
        }

        if (slot.getStatus() == SlotStatus.AVAILABLE) {
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            if (slot.getStartTime().isBefore(LocalDateTime.now().plusHours(8))) {
                throw new TooBigDifferenceBetweenOriginalDateException();
            }

            long startDiffMinutes = Math.abs(Duration.between(startTime, slot.getStartTime()).toMinutes());
            long endDiffMinutes = Math.abs(Duration.between(endTime, slot.getEndTime()).toMinutes());

            if (startDiffMinutes > 120 || endDiffMinutes > 120) {
                throw new TooBigDifferenceBetweenOriginalDateException();
            }

            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
        }

        TrainingSlot changedSlot = trainingSlotRepository.save(slot);
        return trainingSlotCoachDTOMapper.toDto(changedSlot);
    }

    // GET TRAINING PLAN ALL
    public Page<TrainingSlotClientDTO> findTrainingSlotsForClient(
            TrainingSlotUserFilter filter,
            Pageable pageable) {

        filter.setStatus(SlotStatus.AVAILABLE);

        return trainingSlotRepository
                .findAll(TrainingSlotUserSpecification.filter(filter), pageable)
                .map(trainingSlotClientDTOMapper::toDto);
    }

    public Page<TrainingSlotClientDTO> findAllTrainingSlotsOfCoach(
            String coachUsername,
            TrainingSlotUserFilter filter,
            Pageable pageable){

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (coach.getRole() != Role.COACH){
            throw new WrongRoleException();
        }

        filter.setExactCoachUsername(coachUsername);
        filter.setStatus(SlotStatus.AVAILABLE);

        return trainingSlotRepository
                .findAll(TrainingSlotUserSpecification.filter(filter), pageable)
                .map(trainingSlotClientDTOMapper::toDto);
    }

    // GET COACH TRAINING PLAN ALL
    public Page<TrainingSlotCoachDTO> findTrainingSlotsForCoach(
            String coachUsername,
            TrainingSlotCoachFilter filter,
            Pageable pageable) {

        User coach = userRepository.findUserByUsernameEqualsIgnoreCase(coachUsername)
                .orElseThrow(() -> new UserNotFoundException(coachUsername));

        if (coach.getRole() != Role.COACH){
            throw new WrongRoleException();
        }

        return trainingSlotRepository
                .findAll(TrainingSlotCoachSpecification.filter(coachUsername, filter), pageable)
                .map(trainingSlotCoachDTOMapper::toDto);
    }
}
