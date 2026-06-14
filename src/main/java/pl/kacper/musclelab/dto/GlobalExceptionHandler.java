package pl.kacper.musclelab.dto;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kacper.musclelab.exception.conflict.UserAlreadyExistException;
import pl.kacper.musclelab.exception.conflict.UserEmailAlreadyExist;
import pl.kacper.musclelab.exception.not_Found.ReservationNotFoundException;
import pl.kacper.musclelab.exception.not_Found.TrainingSlotNotFoundException;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.business.*;
import pl.kacper.musclelab.exception.validation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // BŁĄD 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler (MethodArgumentNotValidException ex){
        String getMessage = ex.getBindingResult()
                              .getFieldErrors()
                              .getFirst()
                              .getDefaultMessage();

        log.warn("Bad Request exception: {}", getMessage);
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message(getMessage)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationExceptionHandler(jakarta.validation.ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation error");

        log.warn("Validation exception: {}", message);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message(message)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StringIndexOutOfBoundsException.class)
    public ResponseEntity<ErrorResponse> stringIndexOutOfBoundExceptionHandler(RuntimeException ex){
        log.warn("Bad request exception: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }



    // BŁĄD 404
    @ExceptionHandler({
            UserNotFoundException.class,
            TrainingSlotNotFoundException.class,
            ReservationNotFoundException.class})
    public ResponseEntity<ErrorResponse> notFoundExceptionHandler(RuntimeException ex){
        log.warn("Not Found exception: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    // BŁĄD 409
    @ExceptionHandler({
            UserAlreadyExistException.class,
            UserEmailAlreadyExist.class
    })
    public ResponseEntity<ErrorResponse> conflictExceptionHandler(RuntimeException ex){
        log.warn("Conflict exception: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("CONFLICT")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // BŁĄD 422
    @ExceptionHandler({
            CantCancelTrainingException.class,
            CantCompleteTrainingBeforeItEndsException.class,
            TooManyTrainingTodayException.class,
            TooManyTrainingThisWeekException.class,
            WrongReservationStatus.class,
            WrongRoleException.class,
            WrongSlotStatusException.class,
            IncorrectNewAgeException.class,
            IncorrectNewBioException.class,
            IncorrectNewEmailException.class,
            IncorrectNewExperienceException.class,
            IncorrectNewSpecialisationException.class,
            IncorrectNewUsernameException.class,
            IncorrectPasswordException.class,
            IncorrectUserException.class,
            PasswordsAreNotTheSameException.class,
            TooBigDifferenceBetweenOriginalDateException.class,
            WrongTrainingDataException.class,
            IncorrectAgeDueToExperienceException.class
    })
    public ResponseEntity<ErrorResponse> unprocessableEntityExceptionHandler(RuntimeException ex){
        log.warn("Unprocessable Entity exception: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("UNPROCESSABLE_ENTITY")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Unexpected error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generalExceptionHandler(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("Unexpected server error")
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
