package pl.kacper.musclelab.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.musclelab.dto.create.CreateReservation;
import pl.kacper.musclelab.dto.entity.ReservationClientDTO;
import pl.kacper.musclelab.dto.entity.ReservationCoachDTO;
import pl.kacper.musclelab.dto.filter.ReservationClientFilter;
import pl.kacper.musclelab.dto.filter.ReservationCoachFilter;
import pl.kacper.musclelab.service.ReservationService;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    // POST
    @PostMapping()
    public ResponseEntity<ReservationClientDTO> makeReservation(@Valid @RequestBody CreateReservation createReservation,
                                                                Authentication authentication) {
        String clientUsername = authentication.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.makeReservation(createReservation, clientUsername));
    }

    // PUT
    @PutMapping("/client/cancel")
    public ResponseEntity<ReservationClientDTO> cancelReservationAsAClient(@RequestParam String reservationCode,
                                                                           Authentication authentication) {
        String clientUsername = authentication.getName();

        return ResponseEntity.ok(reservationService.cancelReservationAsClient(reservationCode, clientUsername));
    }

    @PutMapping("/coach/cancel")
    public ResponseEntity<ReservationCoachDTO> cancelReservationAsACoach(@RequestParam String reservationCode,
                                                                         Authentication authentication){

        String coachUsername = authentication.getName();
        return ResponseEntity.ok(reservationService.cancelReservationAsCoach(reservationCode, coachUsername));
    }

    @PutMapping("/coach/complete")
    public ResponseEntity<ReservationCoachDTO> completeTrainingAsACoach(@RequestParam String reservationCode,
                                                                        Authentication authentication){

        String coachUsername = authentication.getName();
        return ResponseEntity.ok(reservationService.completeTrainingAsCoach(reservationCode, coachUsername));
    }

    // GET
    @GetMapping("/client")
    public ResponseEntity<Page<ReservationClientDTO>> showAllReservationForClient(Authentication authentication,
                                                                                  @ModelAttribute ReservationClientFilter filter,
                                                                                  Pageable pageable) {
        String username = authentication.getName();
        return ResponseEntity.ok(reservationService.showAllReservationForClient(username, filter, pageable));
    }

    @GetMapping("/coach")
    public ResponseEntity<Page<ReservationCoachDTO>> showAllReservationForCoach(Authentication authentication,
                                                                                @ModelAttribute ReservationCoachFilter filter,
                                                                                Pageable pageable){

        String username = authentication.getName();
        return ResponseEntity.ok(reservationService.showAllReservationForCoach(username, filter, pageable));
    }
}
