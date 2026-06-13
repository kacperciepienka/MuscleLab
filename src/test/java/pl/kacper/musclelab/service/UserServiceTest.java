package pl.kacper.musclelab.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kacper.musclelab.dto.UserLogin;
import pl.kacper.musclelab.dto.create.CreateUser;
import pl.kacper.musclelab.dto.mainView.CoachMainViewDTO;
import pl.kacper.musclelab.dto.mainView.UserMainViewDTO;
import pl.kacper.musclelab.exception.business.WrongRoleException;
import pl.kacper.musclelab.exception.conflict.UserAlreadyExistException;
import pl.kacper.musclelab.exception.conflict.UserEmailAlreadyExist;
import pl.kacper.musclelab.exception.validation.*;
import pl.kacper.musclelab.mapper.create.CreateUserMapper;
import pl.kacper.musclelab.mapper.mainView.CoachMainViewMapper;
import pl.kacper.musclelab.mapper.mainView.UserMainViewMapper;
import pl.kacper.musclelab.model.Role;
import pl.kacper.musclelab.model.User;
import pl.kacper.musclelab.repository.ReservationRepository;
import pl.kacper.musclelab.repository.TrainingSlotRepository;
import pl.kacper.musclelab.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMainViewMapper userMainViewMapper;

    @Mock
    private CreateUserMapper createUserMapper;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrainingSlotRepository trainingSlotRepository;

    @Mock
    private CoachMainViewMapper coachMainViewMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Nested
    @DisplayName("Testy rejestracji klienta")
    class CreateUserTests {

        @Test
        @DisplayName("Rejestracja (Happy path)")
        void shouldAddClient() {
            CreateUser createUser = CreateUser.builder()
                    .username("KaraP")
                    .email("karol.nOWAk@gmail.com   ")
                    .password("testoweHaslo")
                    .firstName("Karol ")
                    .lastName(" Nowak")
                    .age(22)
                    .experience(3)
                    .build();

            User user = User.builder()
                    .username("KaraP")
                    .email("karol.nOWAk@gmail.com   ")
                    .firstName("Karol ")
                    .password("testoweHaslo")
                    .lastName(" Nowak")
                    .age(22)
                    .experience(3)
                    .build();

            UserMainViewDTO expectedDto = UserMainViewDTO.builder()
                    .userId("KA-123456789877")
                    .username("karap")
                    .email("karol.nowak@gmail.com")
                    .firstName("Karol")
                    .lastName("Nowak")
                    .age(22)
                    .experience(3)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(createUser.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmailEqualsIgnoreCase(createUser.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("testoweHaslo")).thenReturn("encodedPassword");
            when(createUserMapper.toEntity(createUser)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(expectedDto);

            UserMainViewDTO result = userService.createClient(createUser);

            assertAll("testy",
                    () -> assertEquals("karap", result.getUsername()),
                    () -> assertEquals("karol.nowak@gmail.com", result.getEmail()),
                    () -> assertEquals("Karol", result.getFirstName()),
                    () -> assertEquals("Nowak", result.getLastName()),
                    () -> assertEquals(22, result.getAge()),
                    () -> assertEquals(3, result.getExperience())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(createUser.getUsername());
            verify(userRepository, times(1)).findUserByEmailEqualsIgnoreCase(createUser.getEmail());
            verify(passwordEncoder, times(1)).encode("testoweHaslo");
            verify(createUserMapper, times(1)).toEntity(createUser);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @Test
        @DisplayName("Rejestracja (Fail path - Username alreadyExist)")
        void shouldThrowUserAlreadyExistException() {

            User user = User.builder()
                    .username("piotrolo")
                    .email("piotr@wp.pl")
                    .build();

            CreateUser createUser = CreateUser.builder()
                    .username("piotrolo")
                    .email("piotr.gg@wp.pl")
                    .firstName("Piotr")
                    .lastName("Grzejszczyk")
                    .age(44)
                    .experience(33)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(createUser.getUsername())).thenReturn(Optional.of(user));


            assertThrows(UserAlreadyExistException.class,
                    () -> userService.createClient(createUser));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(createUser.getUsername());

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Rejestracja (Fail path - Email alreadyExist)")
        void shouldThrowUserEmailAlreadyExist() {
            User user = User.builder()
                    .username("pitrek")
                    .email("piotr.gg@wp.pl")
                    .build();

            CreateUser createUser = CreateUser.builder()
                    .username("piotrolo")
                    .email("piotr.gg@wp.pl")
                    .firstName("Piotr")
                    .lastName("Grzejszczyk")
                    .age(44)
                    .experience(3)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(createUser.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmailEqualsIgnoreCase(createUser.getEmail())).thenReturn(Optional.of(user));

            assertThrows(UserEmailAlreadyExist.class,
                    () -> userService.createClient(createUser));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(createUser.getUsername());
            verify(userRepository, times(1)).findUserByEmailEqualsIgnoreCase(createUser.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Rejestracja (Fail path - experience > age)")
        void shouldThrowIncorrectAgeDueToExperienceException() {
            CreateUser createUser = CreateUser.builder()
                    .username("ripza23")
                    .email("ageszka@wp.pl")
                    .firstName("Agnieszka")
                    .lastName("Grzejszczyk")
                    .age(22)
                    .experience(33)
                    .build();

            User user = User.builder()
                    .username("ripza23")
                    .email("ageszka@wp.pl")
                    .firstName("Agnieszka")
                    .lastName("Grzejszczyk")
                    .age(22)
                    .experience(33)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(createUser.getUsername())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmailEqualsIgnoreCase(createUser.getEmail())).thenReturn(Optional.empty());
            when(createUserMapper.toEntity(createUser)).thenReturn(user);

            assertThrows(IncorrectAgeDueToExperienceException.class,
                    () -> userService.createClient(createUser));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(createUser.getUsername());
            verify(userRepository, times(1)).findUserByEmailEqualsIgnoreCase(createUser.getEmail());
            verify(createUserMapper, times(1)).toEntity(createUser);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Testy logowania klienta")
    class LoginUserTest {

        @Test
        @DisplayName("Login client (happy path)")
        void shouldLoginUser() {
            UserLogin userLogin = UserLogin.builder()
                    .username("test123")
                    .password("qwerty123")
                    .build();

            User user = User.builder()
                    .username("test123")
                    .email("test@wp.pl")
                    .password("qwerty123")
                    .firstName("test")
                    .lastName("testowy")
                    .role(Role.CLIENT)
                    .age(33)
                    .experience(3)
                    .build();

            UserMainViewDTO mainViewDTO = UserMainViewDTO.builder()
                    .username("test123")
                    .email("test@wp.pl")
                    .firstName("test")
                    .lastName("testowy")
                    .age(33)
                    .experience(3)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(userLogin.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(userLogin.getPassword(), user.getPassword())).thenReturn(true);
            when(userMainViewMapper.toDto(user)).thenReturn(mainViewDTO);

            Object result = userService.loginUser(userLogin);
            UserMainViewDTO dto = (UserMainViewDTO) result;

            assertAll("test",
                    () -> assertEquals("test123", dto.getUsername()),
                    () -> assertEquals("test@wp.pl", dto.getEmail()),
                    () -> assertEquals("test", dto.getFirstName()),
                    () -> assertEquals(3, dto.getExperience())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(userLogin.getUsername());
            verify(passwordEncoder, times(1)).matches(userLogin.getPassword(), user.getPassword());
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @Test
        @DisplayName("Login client (Fail path - bad password)")
        void shouldThrowIncorrectPasswordException() {
            UserLogin userLogin = UserLogin.builder()
                    .username("test123")
                    .password("testowe333")
                    .build();

            User user = User.builder()
                    .username("test123")
                    .email("test@wp.pl")
                    .password("encodedPassword")
                    .firstName("test")
                    .lastName("testowy")
                    .role(Role.CLIENT)
                    .age(33)
                    .experience(3)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(userLogin.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(userLogin.getPassword(), user.getPassword())).thenReturn(false);

            assertThrows(IncorrectPasswordException.class,
                    () -> userService.loginUser(userLogin));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(userLogin.getUsername());
            verify(passwordEncoder, times(1)).matches(userLogin.getPassword(), user.getPassword());
            verify(userMainViewMapper, never()).toDto(user);
        }
    }


    @Nested
    @DisplayName("Testy usuwania konta")
    class DeleteUserTest {

        @Test
        @DisplayName("Delete user (Happy path - client)")
        void shouldDeleteClientAccount() {
            String username = "client123";
            String password = "qwerty123";

            User client = User.builder()
                    .id(1L)
                    .username(username)
                    .password("encodedPassword")
                    .role(Role.CLIENT)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(username)).thenReturn(Optional.of(client));
            when(passwordEncoder.matches(password, client.getPassword())).thenReturn(true);

            userService.deleteUser(username, password);

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(passwordEncoder, times(1)).matches(password, "encodedPassword");
            verify(trainingSlotRepository, times(1)).makeBookedSlotsAvailableForClient(1L);
            verify(reservationRepository, times(1)).deleteAllByClientId(1L);
            verify(userRepository, times(1)).deleteById(1L);
            verify(reservationRepository, never()).deleteAllByCoachId(anyLong());
            verify(trainingSlotRepository, never()).deleteAllByCoachId(anyLong());
        }

        @Test
        @DisplayName("Delete coach (Happy path - client)")
        void shouldDeleteCoachAccount(){
            String username = "coach12";
            String password = "password123";

            User user = User.builder()
                    .id(1L)
                    .username(username)
                    .password("encodedPassword")
                    .role(Role.COACH)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

            userService.deleteUser(user.getUsername(), password);

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(passwordEncoder, times(1)).matches(password, user.getPassword());
            verify(reservationRepository, times(1)).deleteAllByCoachId(1L);
            verify(trainingSlotRepository, times(1)).deleteAllByCoachId(1L);
            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Delete client (Fail path - passwords are not the same)")
        void shouldThrowIncorrectPasswordException(){
            String username = "client837";
            String password = "password123";

            User user = User.builder()
                    .id(1L)
                    .username(username)
                    .password("encodedPassword")
                    .role(Role.CLIENT)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

            assertThrows(IncorrectPasswordException.class,
                    () -> userService.deleteUser(username, password));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(passwordEncoder, times(1)).matches(password, user.getPassword());
            verify(trainingSlotRepository, never()).makeBookedSlotsAvailableForClient(1L);
            verify(reservationRepository, never()).deleteAllByClientId(1L);
            verify(userRepository, never()).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Testy zmiany username")
    class ChangeUsernameTest {

        @Test
        @DisplayName("Change username (Happy path)")
        void shouldChangeUsername() {
            String oldUsername = "leo333";
            String newUsername = "Mike030";


            User user = User.builder()
                    .username(oldUsername)
                    .email("michap@wp.pl")
                    .password("uu032")
                    .firstName("Piotr")
                    .build();

            UserMainViewDTO userMainViewDTO = UserMainViewDTO.builder()
                    .username("mike030")
                    .email("michap@wp.pl")
                    .firstName("Piotr")
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(oldUsername)).thenReturn(Optional.of(user));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(newUsername)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(userMainViewDTO);

            Object result = userService.changeUsername(oldUsername, newUsername);
            UserMainViewDTO dto = (UserMainViewDTO) result;

            assertAll("test",
                    () -> assertEquals("mike030", dto.getUsername()),
                    () -> assertEquals("michap@wp.pl", dto.getEmail()),
                    () -> assertEquals("Piotr", dto.getFirstName()),
                    () -> assertEquals("mike030", user.getUsername())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(oldUsername);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(newUsername);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @ParameterizedTest
        @DisplayName("Change username (Fail path - bad usernames)")
        @ValueSource(strings = {"", "d", " ", "huiterhfiehfipefhnreoifhefkge7bfgryidbsfgyeurgfyildygfeoufveufvuecolvealruhbveairufbhveujrvfaechuaregvcuelibcruy"})
        void shouldThrowIncorrectNewUsernameException(String newUsername) {
            String oldUsername = "Jacek883";

            assertThrows(IncorrectNewUsernameException.class,
                    () -> userService.changeUsername(oldUsername, newUsername));
        }

        @Test
        @DisplayName("Change username (Fail path - new username already exist)")
        void shouldThrowUserAlreadyExistException() {
            String oldUsername = "leo333";
            String newUsername = "Mike030";


            User user = User.builder()
                    .username(oldUsername)
                    .email("michap@wp.pl")
                    .password("uu032")
                    .firstName("Piotr")
                    .build();

            User user2 = User.builder()
                    .username(newUsername)
                    .email("gawel@wp.pl")
                    .password("848ujo")
                    .firstName("Michał")
                    .build();


            when(userRepository.findUserByUsernameEqualsIgnoreCase(oldUsername)).thenReturn(Optional.of(user));
            when(userRepository.findUserByUsernameEqualsIgnoreCase(newUsername)).thenReturn(Optional.of(user2));

            assertThrows(UserAlreadyExistException.class,
                    () -> userService.changeUsername(oldUsername, newUsername));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(oldUsername);
            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(newUsername);
            verify(userRepository, never()).save(user);
        }
    }

    @Nested
    @DisplayName("Testy zmiany email")
    class ChangeEmailTest {

        @Test
        @DisplayName("Change email (Happy path)")
        void shouldChangeEmail() {
            String username = "milena22";
            String newEmail = "MicHal.ppl@wp.pl";

            User user = User.builder()
                    .username(username)
                    .email("milena.729@wp.pl")
                    .firstName("milena")
                    .build();

            UserMainViewDTO userMainViewDTO = UserMainViewDTO.builder()
                    .username(username)
                    .email("michal.ppl@wp.pl")
                    .firstName("milena")
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.findUserByEmailEqualsIgnoreCase(newEmail)).thenReturn(Optional.empty());
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(userMainViewDTO);

            Object result = userService.changeEmail(username, newEmail);
            UserMainViewDTO dto = (UserMainViewDTO) result;

            assertAll("test",
                    () -> assertEquals(username, dto.getUsername()),
                    () -> assertEquals("michal.ppl@wp.pl", dto.getEmail()),
                    () -> assertEquals("milena", dto.getFirstName())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(userRepository, times(1)).findUserByEmailEqualsIgnoreCase(newEmail);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @ParameterizedTest
        @DisplayName("Change email (Fail path - invalid new email values)")
        @ValueSource(strings = {" ", "jduioauhidhsihfy8oe47g83boybfysbeghufjfuiuy46dkajenyb8fog3784gbuydgsfiubdsvfkuyfgy8fuwbfhuwbyfb@wp.pl", "d@wp", ""})
        void shouldThrowIncorrectNewEmailException(String newEmail) {
            String username = "pawel222";

            assertThrows(IncorrectNewEmailException.class,
                    () -> userService.changeEmail(username, newEmail));
        }

        @Test
        @DisplayName("Change email (Fail path - new Email already exist)")
        void shouldThrowUserEmailAlreadyExist() {

            String oldEmail = "djikke@wp.pl";
            String newEmail = "hufi84@wp.pl";

            User user1 = User.builder()
                    .username("pawel123")
                    .email(oldEmail)
                    .firstName("pawel")
                    .build();

            User user2 = User.builder()
                    .username("adrian333")
                    .email(newEmail)
                    .firstName("adrian")
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user1.getUsername())).thenReturn(Optional.of(user1));
            when(userRepository.findUserByEmailEqualsIgnoreCase(newEmail)).thenReturn(Optional.of(user2));

            assertThrows(UserEmailAlreadyExist.class,
                    () -> userService.changeEmail(user1.getUsername(), newEmail));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(user1.getUsername());
            verify(userRepository, times(1)).findUserByEmailEqualsIgnoreCase(newEmail);
        }
    }

    @Nested
    @DisplayName("Testy zmiany hasła")
    class PasswordChangeTest {

        @Test
        @DisplayName("Change password (Happy path)")
        void shouldChangePassword() {
            String username = "test";
            String oldPassword = "123456789";
            String newPassword = "qwerty123";

            User user = User.builder()
                    .username(username)
                    .email("testowy@wp.pl")
                    .password("encodedOldPassword")
                    .firstName("Adrian")
                    .build();

            UserMainViewDTO userMainViewDTO = UserMainViewDTO.builder()
                    .username(username)
                    .email("testowy@wp.pl")
                    .firstName("Adrian")
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(userMainViewDTO);

            Object result = userService.changePassword(username, oldPassword, newPassword);
            UserMainViewDTO dto = (UserMainViewDTO) result;

            assertAll("test",
                    () -> assertEquals(username, dto.getUsername()),
                    () -> assertEquals("testowy@wp.pl", dto.getEmail()),
                    () -> assertEquals("Adrian", dto.getFirstName())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(passwordEncoder, times(1)).matches(oldPassword, "encodedOldPassword");
            verify(passwordEncoder, times(1)).encode(newPassword);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);

            assertEquals("encodedNewPassword", user.getPassword());
        }

        @ParameterizedTest
        @DisplayName("Change password (Fail path - invalid password lengths)")
        @ValueSource(strings = {" ", "", "333d3"})
        void shouldThrowIncorrectPasswordException(String newPassword) {
            assertThrows(IncorrectPasswordException.class,
                    () -> userService.changePassword("janek", "qwerty123", newPassword));
        }

        @Test
        @DisplayName("Change password (Fail path - old password incorrect)")
        void shouldThrowPasswordsAreNotTheSameException() {
            String username = "test";
            String oldPassword = "123456789";
            String newPassword = "qwerty123";

            User user = User.builder()
                    .username(username)
                    .email("testowy@wp.pl")
                    .password("encodedOldPassword")
                    .firstName("Adrian")
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(oldPassword, "encodedOldPassword")).thenReturn(false);

            assertThrows(PasswordsAreNotTheSameException.class,
                    () -> userService.changePassword(username, oldPassword, newPassword));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(passwordEncoder, times(1)).matches(oldPassword, "encodedOldPassword");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Testy zmiany wieku")
    class AgeChangeTest {

        @Test
        @DisplayName("Change age (Happy path)")
        void shouldChangeAge() {
            String username = "luki";
            int oldAge = 22;
            int newAge = 23;

            User user = User.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(oldAge)
                    .experience(2)
                    .build();

            UserMainViewDTO userMainViewDTO = UserMainViewDTO.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(newAge)
                    .experience(2)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(userMainViewDTO);

            Object result = userService.updateAge(username, newAge);
            UserMainViewDTO dto = (UserMainViewDTO) result;

            assertAll("test",
                    () -> assertEquals("luki", dto.getUsername()),
                    () -> assertEquals("lukasz22@wp.pl", dto.getEmail()),
                    () -> assertEquals("lukasz", dto.getFirstName()),
                    () -> assertEquals(23, dto.getAge()),
                    () -> assertEquals(2, dto.getExperience()),
                    () -> assertEquals(23, user.getAge())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @ParameterizedTest
        @DisplayName("Change age (Fail path - invalid age)")
        @ValueSource(ints = {9, 101, 0, -1})
        void shouldThrowIncorrectNewAgeException(int newAge) {
            assertThrows(IncorrectNewAgeException.class,
                    () -> userService.updateAge("luki", newAge));
        }

        @ParameterizedTest
        @DisplayName("Change age (Fail path - invalid age)")
        @ValueSource(ints = {12, 26, 25})
        void shouldThrowIncorrectNewAgeExceptionTooLowAge(int newAge) {
            String username = "luki";
            int oldAge = 59;

            User user = User.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(oldAge)
                    .experience(26)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

            assertThrows(IncorrectAgeDueToExperienceException.class,
                    () -> userService.updateAge(username, newAge));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
        }
    }

    @Nested
    @DisplayName("Testy zmiany doświadczenia")
    class ExperienceChangeTest {

        @Test
        @DisplayName("Change experience (Happy path)")
        void shouldUpdateExperience() {
            String username = "luki";
            int oldExperience = 2;
            int newExperience = 3;

            User user = User.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(22)
                    .experience(oldExperience)
                    .build();

            UserMainViewDTO userMainViewDTO = UserMainViewDTO.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(22)
                    .experience(newExperience)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMainViewMapper.toDto(user)).thenReturn(userMainViewDTO);

            Object result = userService.updateExperience(username, newExperience);
            UserMainViewDTO dto = (UserMainViewDTO) result;
            assertAll("test",
                    () -> assertEquals("luki", dto.getUsername()),
                    () -> assertEquals("lukasz22@wp.pl", dto.getEmail()),
                    () -> assertEquals("lukasz", dto.getFirstName()),
                    () -> assertEquals(22, dto.getAge()),
                    () -> assertEquals(3, dto.getExperience()),
                    () -> assertEquals(3, user.getExperience())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(userRepository, times(1)).save(user);
            verify(userMainViewMapper, times(1)).toDto(user);
        }

        @ParameterizedTest
        @DisplayName("Change experience (Fail path - invalid experience)")
        @ValueSource(ints = {101, -22, -1})
        void shouldThrowIncorrectNewAgeException(int newExperience) {
            assertThrows(IncorrectNewExperienceException.class,
                    () -> userService.updateExperience("luki", newExperience));
        }

        @ParameterizedTest
        @DisplayName("Change experience (Fail path - invalid experience)")
        @ValueSource(ints = {18, 19, 99})
        void shouldThrowIncorrectNewAgeExceptionTooLowAge(int newExperience) {
            String username = "luki";
            int oldExperience = 59;

            User user = User.builder()
                    .username(username)
                    .email("lukasz22@wp.pl")
                    .firstName("lukasz")
                    .age(18)
                    .experience(oldExperience)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

            assertThrows(IncorrectAgeDueToExperienceException.class,
                    () -> userService.updateExperience(username, newExperience));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
        }
    }

    @Nested
    @DisplayName("Testy zmiany specjalizacji")
    class SpecialisationChangeTest {

        @Test
        @DisplayName("Change specialisation (Happy path)")
        void shouldUpdateExperience() {
            String username = "pablo";
            String email = "pablo@wp.pl";
            String firstName = "pawel";

            String oldSpecialisation = "Powerlifter";
            String newSpecialisation = "Bodybuilder";
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .specialisation(oldSpecialisation)
                    .role(Role.COACH)
                    .build();

            CoachMainViewDTO coachMainViewDTO = CoachMainViewDTO.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .specialisation(newSpecialisation)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(coachMainViewMapper.toDto(user)).thenReturn(coachMainViewDTO);

            CoachMainViewDTO result = userService.updateSpecialisation(username, newSpecialisation);
            assertAll("test",
                    () -> assertEquals("pablo", result.getUsername()),
                    () -> assertEquals("pablo@wp.pl", result.getEmail()),
                    () -> assertEquals("pawel", result.getFirstName()),
                    () -> assertEquals("Bodybuilder", result.getSpecialisation()),
                    () -> assertEquals("bodybuilder", user.getSpecialisation())
            );

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(userRepository, times(1)).save(user);
            verify(coachMainViewMapper, times(1)).toDto(user);
        }

        @ParameterizedTest
        @DisplayName("Change specialisation (Fail path - invalid specialisation)")
        @ValueSource(strings = {"", " ", "s", "huydgyufgsyfwjegfuyewgfyiuewgfuyeguiwegcyudoshcu8wgciwuygucigwuiodghwyugdbwibcwic"})
        void shouldThrowIncorrectNewSpecialisationException(String specialisation) {
            assertThrows(IncorrectNewSpecialisationException.class,
                    () -> userService.updateSpecialisation("pablo", specialisation));
        }

        @Test
        @DisplayName("Change specialisation (Fail path - wrong role)")
        void shouldThrowWrongRoleException() {
            String username = "pablo";

            String oldSpecialisation = "Powerlifter";
            String newSpecialisation = "Bodybuilder";

            User user = User.builder()
                    .username(username)
                    .specialisation(oldSpecialisation)
                    .role(Role.CLIENT)
                    .build();

            when(userRepository.findUserByUsernameEqualsIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));

            assertThrows(WrongRoleException.class,
                    () -> userService.updateSpecialisation(username, newSpecialisation));

            verify(userRepository, times(1)).findUserByUsernameEqualsIgnoreCase(username);
            verify(userRepository, never()).save(user);
            verify(coachMainViewMapper, never()).toDto(user);
        }
    }
}

