package pl.kacper.musclelab.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.musclelab.dto.UserLogin;
import pl.kacper.musclelab.dto.create.CreateUser;
import pl.kacper.musclelab.dto.entity.CoachPublicDTO;
import pl.kacper.musclelab.dto.entity.UserCoachDTO;
import pl.kacper.musclelab.dto.filter.CoachClientFilter;
import pl.kacper.musclelab.dto.filter.UserCoachFilter;
import pl.kacper.musclelab.dto.mainView.CoachMainViewDTO;
import pl.kacper.musclelab.dto.mainView.UserMainViewDTO;
import pl.kacper.musclelab.service.UserService;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    // POST
    @PostMapping("/clients")
    public ResponseEntity<UserMainViewDTO> createClient(@Valid @RequestBody CreateUser client) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createClient(client));
    }

    @PostMapping("/coaches")
    public ResponseEntity<CoachMainViewDTO> createCoach(@Valid @RequestBody CreateUser coach) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createCoach(coach));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> loginUser(@Valid @RequestBody UserLogin login) {
        return ResponseEntity.ok(userService.loginUser(login));
    }

    // DELETE
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(Authentication authentication,
                                           @RequestParam String password) {
        String username = authentication.getName();

        userService.deleteUser(username, password);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // PUT
    @PutMapping("/username")
    public ResponseEntity<Object> changeUsername(Authentication authentication,
                                                 @RequestParam String newUsername) {

        String oldUsername = authentication.getName();
        return ResponseEntity.ok(userService.changeUsername(oldUsername, newUsername));
    }

    @PutMapping("/email")
    public ResponseEntity<Object> changeEmail(Authentication authentication,
                                              @RequestParam String newEmail) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.changeEmail(username, newEmail));
    }

    @PutMapping("/password")
    public ResponseEntity<Object> changePassword(Authentication authentication,
                                                 @RequestParam String oldPassword,
                                                 @RequestParam String newPassword) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.changePassword(username, oldPassword, newPassword));
    }

    @PutMapping("/age")
    public ResponseEntity<Object> changeAge(Authentication authentication,
                                            @RequestParam Integer age) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateAge(username, age));
    }

    @PutMapping("/experience")
    public ResponseEntity<Object> changeExperience(Authentication authentication,
                                                   @RequestParam Integer experience) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateExperience(username, experience));
    }

    @PutMapping("/bio")
    public ResponseEntity<Object> changeBio(Authentication authentication,
                                            @RequestParam String bio) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.changeBio(username, bio));
    }

    @PutMapping("/specialisation")
    public ResponseEntity<CoachMainViewDTO> changeSpecialisation(Authentication authentication,
                                                                 @RequestParam String specialisation) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.updateSpecialisation(username, specialisation));
    }

    // GET
    @GetMapping("/coaches")
    public ResponseEntity<Page<CoachPublicDTO>> findCoaches(@ModelAttribute CoachClientFilter filter,
                                                            Pageable pageable) {
        return ResponseEntity.ok(userService.findCoaches(filter, pageable));
    }

    @GetMapping("/clients")
    public ResponseEntity<Page<UserCoachDTO>> findUsers(@ModelAttribute UserCoachFilter filter,
                                                        Pageable pageable) {
        return ResponseEntity.ok(userService.findClients(filter, pageable));
    }

    @GetMapping("/clients/me")
    public ResponseEntity<UserMainViewDTO> getClientMainView(Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(userService.getClientMainView(username));
    }

    @GetMapping("/coaches/me")
    public ResponseEntity<CoachMainViewDTO> getCoachMainView(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getCoachMainView(username));
    }
}
