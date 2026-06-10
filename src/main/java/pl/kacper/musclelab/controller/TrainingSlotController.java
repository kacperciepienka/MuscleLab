package pl.kacper.musclelab.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.musclelab.dto.create.CreateTrainingSlot;
import pl.kacper.musclelab.dto.entity.TrainingSlotClientDTO;
import pl.kacper.musclelab.dto.entity.TrainingSlotCoachDTO;
import pl.kacper.musclelab.dto.filter.TrainingSlotCoachFilter;
import pl.kacper.musclelab.dto.filter.TrainingSlotUserFilter;
import pl.kacper.musclelab.service.TrainingSlotService;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/training-slots")
public class TrainingSlotController {

    private final TrainingSlotService trainingSlotService;

    // POST
    @PostMapping()
    public ResponseEntity<TrainingSlotCoachDTO> createTrainingSlot(@Valid @RequestBody CreateTrainingSlot newTrainingSlot,
                                                                   Authentication authentication) {
        String coachUsername = authentication.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingSlotService.createTrainingSlot(newTrainingSlot, coachUsername));
    }

    // PUT
    @PutMapping("/cancel")
    public ResponseEntity<TrainingSlotCoachDTO> cancelSlot(@RequestParam String slotCode,
                                                           Authentication authentication) {

        String coachUsername = authentication.getName();
        return ResponseEntity.ok(trainingSlotService.cancelSlot(slotCode, coachUsername));
    }

    @PutMapping("/date")
    public ResponseEntity<TrainingSlotCoachDTO> updateTrainingTime(@RequestParam String slotCode,
                                                                   Authentication authentication,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        String coachUsername = authentication.getName();
        return ResponseEntity.ok(trainingSlotService.updateTrainingTime(slotCode, coachUsername, startTime, endTime));
    }

    // GET
    @GetMapping("/client")
    public ResponseEntity<Page<TrainingSlotClientDTO>> userFindTrainingSlots(@ModelAttribute TrainingSlotUserFilter filter,
                                                                             Pageable pageable) {
        return ResponseEntity.ok(trainingSlotService.findTrainingSlotsForClient(filter, pageable));
    }

    @GetMapping("/client/{coachUsername}")
    public ResponseEntity<Page<TrainingSlotClientDTO>> userFindTrainingSlotsByCoachUsername(@PathVariable String coachUsername,
                                                                                            @ModelAttribute TrainingSlotUserFilter filter,
                                                                                            Pageable pageable) {
        return ResponseEntity.ok(trainingSlotService.findAllTrainingSlotsOfCoach(coachUsername, filter, pageable));
    }

    @GetMapping("/coach")
    public ResponseEntity<Page<TrainingSlotCoachDTO>> coachFindTrainingSlots(Authentication authentication,
                                                                             @ModelAttribute TrainingSlotCoachFilter filter,
                                                                             Pageable pageable) {

        String coachUsername = authentication.getName();
        return ResponseEntity.ok(trainingSlotService.findTrainingSlotsForCoach(coachUsername, filter, pageable));
    }
}
