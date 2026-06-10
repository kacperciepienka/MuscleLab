package pl.kacper.musclelab.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kacper.musclelab.dto.UserLogin;
import pl.kacper.musclelab.dto.create.CreateUser;
import pl.kacper.musclelab.dto.entity.CoachPublicDTO;
import pl.kacper.musclelab.dto.entity.UserCoachDTO;
import pl.kacper.musclelab.dto.filter.CoachClientFilter;
import pl.kacper.musclelab.dto.filter.UserCoachFilter;
import pl.kacper.musclelab.dto.mainView.CoachMainViewDTO;
import pl.kacper.musclelab.dto.mainView.UserMainViewDTO;
import pl.kacper.musclelab.exception.conflict.UserAlreadyExistException;
import pl.kacper.musclelab.exception.conflict.UserEmailAlreadyExist;
import pl.kacper.musclelab.exception.not_Found.UserNotFoundException;
import pl.kacper.musclelab.exception.business.WrongRoleException;
import pl.kacper.musclelab.exception.validation.*;
import pl.kacper.musclelab.mapper.create.CreateUserMapper;
import pl.kacper.musclelab.mapper.entity.CoachPublicDTOMapper;
import pl.kacper.musclelab.mapper.entity.UserCoachDTOMapper;
import pl.kacper.musclelab.mapper.mainView.CoachMainViewMapper;
import pl.kacper.musclelab.mapper.mainView.UserMainViewMapper;
import pl.kacper.musclelab.model.Role;
import pl.kacper.musclelab.model.User;
import pl.kacper.musclelab.repository.UserRepository;
import pl.kacper.musclelab.specification.CoachClientSpecification;
import pl.kacper.musclelab.specification.UserCoachSpecification;
import pl.kacper.musclelab.repository.ReservationRepository;
import pl.kacper.musclelab.repository.TrainingSlotRepository;


import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TrainingSlotRepository trainingSlotRepository;

    private final UserCoachDTOMapper userCoachDTOMapper;
    private final UserMainViewMapper userMainViewMapper;

    private final CreateUserMapper createUserMapper;

    private final CoachPublicDTOMapper coachPublicDTOMapper;
    private final CoachMainViewMapper coachMainViewMapper;

    private final PasswordEncoder passwordEncoder;

    Random random = new Random();

    // (POST)
    // rejestracja dla Client
    public UserMainViewDTO createClient(CreateUser clientCreate) {
        userRepository.findUserByUsernameEqualsIgnoreCase(clientCreate.getUsername())
                .ifPresent(u -> {
                    throw new UserAlreadyExistException(clientCreate.getUsername());
                });

        userRepository.findUserByEmailEqualsIgnoreCase(clientCreate.getEmail())
                .ifPresent(u -> {
                    throw new UserEmailAlreadyExist(clientCreate.getEmail());
                });

        // zamiana z create na encje
        User user = createUserMapper.toEntity(clientCreate);

        if (user.getAge() <= user.getExperience()){
            throw new IncorrectAgeDueToExperienceException();
        }

        // tworzenie unikalnego numeru
        int randomNumber = random.nextInt(1000000, 9999999);
        String prefix = clientCreate.getUsername().trim().toUpperCase().substring(0, 2);
        int age = clientCreate.getAge();
        String userId = prefix + "-" + randomNumber + age;

        user.setUserId(userId);
        user.setRole(Role.CLIENT);
        user.setUsername(user.getUsername().trim().toLowerCase());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return userMainViewMapper.toDto(savedUser);
    }

    // rejestracja dla Coach
    public CoachMainViewDTO createCoach(CreateUser coachCreate) {
        userRepository.findUserByUsernameEqualsIgnoreCase(coachCreate.getUsername())
                .ifPresent(u -> {
                    throw new UserAlreadyExistException(coachCreate.getUsername());
                });

        userRepository.findUserByEmailEqualsIgnoreCase(coachCreate.getEmail())
                .ifPresent(u -> {
                    throw new UserEmailAlreadyExist(coachCreate.getEmail());
                });

        User user = createUserMapper.toEntity(coachCreate);

        int randomNumber = random.nextInt(1000000, 9999999);
        String prefix = coachCreate.getUsername().trim().toUpperCase().substring(0, 2);
        int age = coachCreate.getAge();
        String userId = prefix + "-" + randomNumber + age;

        user.setUserId(userId);
        user.setRole(Role.COACH);
        user.setUsername(user.getUsername().trim().toLowerCase());
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return coachMainViewMapper.toDto(savedUser);
    }

    // LOGIN na istniejące konto Client i Coach
    public Object loginUser(UserLogin userToLogin) {
        User user = getUser(userToLogin.getUsername());

        if (!passwordEncoder.matches(userToLogin.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        return mapMainView(user);
    }

    // (DELETE)
    // usuwanie istniejących kont
    public void deleteUser(String username, String password) {
        User user = getUser(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectPasswordException();
        }

        if (user.getRole() == Role.CLIENT) {
            deleteClientAccount(user);
            return;
        }

        if (user.getRole() == Role.COACH) {
            deleteCoachAccount(user);
        }
    }

    // (PUT)
    // zmiana parametrów (wyświetlamy główny widok po zmianie)
    public Object changeUsername(String oldUsername, String newUsername) {
        if (newUsername.isBlank() || newUsername.length() < 3 || newUsername.length() > 100) {
            throw new IncorrectNewUsernameException(newUsername);
        }

        User user = getUser(oldUsername);

        userRepository.findUserByUsernameEqualsIgnoreCase(newUsername)
                .ifPresent(u -> {
                    throw new UserAlreadyExistException(newUsername);
                });

        user.setUsername(newUsername.trim().toLowerCase());

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public Object changeEmail(String username, String newEmail) {
        if (newEmail.isBlank() || newEmail.length() < 5 || newEmail.length() > 100) {
            throw new IncorrectNewEmailException(newEmail);
        }

        User user = getUser(username);

        userRepository.findUserByEmailEqualsIgnoreCase(newEmail)
                .ifPresent(u -> {
                    throw new UserEmailAlreadyExist(newEmail);
                });

        user.setEmail(newEmail.trim().toLowerCase());

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public Object changePassword(String username, String oldPassword, String newPassword) {
        if (newPassword.isBlank() || newPassword.length() < 6 || newPassword.length() > 255) {
            throw new IncorrectPasswordException();
        }

        User user = getUser(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new PasswordsAreNotTheSameException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public Object updateAge(String username, Integer age) {
        if (age < 10 || age > 100) {
            throw new IncorrectNewAgeException(age);
        }

        User user = getUser(username);

        user.setAge(age);

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public Object updateExperience(String username, Integer experience) {
        if (experience < 0 || experience > 100) {
            throw new IncorrectNewExperienceException(experience);
        }

        User user = getUser(username);

        user.setExperience(experience);

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public Object changeBio(String username, String bio) {
        if (bio.isBlank() || bio.length() < 20 || bio.length() > 1000) {
            throw new IncorrectNewBioException();
        }

        User user = getUser(username);

        user.setBio(bio);

        User savedUser = userRepository.save(user);
        return mapMainView(savedUser);
    }

    public CoachMainViewDTO updateSpecialisation(String username, String specialisation) {
        if (specialisation.isBlank() || specialisation.length() < 2 || specialisation.length() > 80) {
            throw new IncorrectNewSpecialisationException(specialisation);
        }

        User user = getUser(username);

        if (user.getRole() != Role.COACH) {
            throw new WrongRoleException();
        }

        user.setSpecialisation(specialisation.trim().toLowerCase());

        User savedUser = userRepository.save(user);
        return coachMainViewMapper.toDto(savedUser);
    }

    // (GET)
    // filtry dla klienta
    public Page<CoachPublicDTO> findCoaches(
            CoachClientFilter coachClientFilter,
            Pageable pageable) {

        return userRepository
                .findAll(CoachClientSpecification.filter(coachClientFilter), pageable)
                .map(coachPublicDTOMapper::toDto);
    }

    // filtry dla trenera
    public Page<UserCoachDTO> findClients(
            UserCoachFilter filter,
            Pageable pageable) {

        return userRepository
                .findAll(UserCoachSpecification.filter(filter), pageable)
                .map(userCoachDTOMapper::toDto);
    }

    // GET /me
    public UserMainViewDTO getClientMainView(String username) {
        User user = getUser(username);
        return userMainViewMapper.toDto(user);
    }

    public CoachMainViewDTO getCoachMainView(String username) {
        User user = getUser(username);
        return coachMainViewMapper.toDto(user);
    }

    // do gromadzenia
    public User getUser(String username) {
        return userRepository.findUserByUsernameEqualsIgnoreCase(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    // wspólne mapowanie widoku głównego po zmianie danych
    private Object mapMainView(User user) {
        if (user.getRole() == Role.COACH) {
            return coachMainViewMapper.toDto(user);
        }

        return userMainViewMapper.toDto(user);
    }

    private void deleteClientAccount(User client) {
        trainingSlotRepository.makeBookedSlotsAvailableForClient(client.getId());

        reservationRepository.deleteAllByClientId(client.getId());

        userRepository.deleteById(client.getId());
    }

    private void deleteCoachAccount(User coach) {
        reservationRepository.deleteAllByCoachId(coach.getId());

        trainingSlotRepository.deleteAllByCoachId(coach.getId());

        userRepository.deleteById(coach.getId());
    }
}