# 💪 MuscleLab

**MuscleLab** to aplikacja webowa wspierająca proces rezerwacji treningów personalnych między trenerami a klientami.

System umożliwia trenerom tworzenie dostępnych terminów treningowych, a klientom przeglądanie trenerów, rezerwowanie treningów oraz zarządzanie swoimi rezerwacjami. Projekt został wykonany jako aplikacja backendowa w architekturze **REST API** z prostym frontendem.

---

## 🎯 Cel projektu

W tradycyjnym modelu umawianie treningów często odbywa się przez wiadomości prywatne, rozmowy lub ręczne ustalanie terminów. Może to prowadzić do pomyłek, braku przejrzystości grafiku oraz trudniejszego kontrolowania historii rezerwacji.

**MuscleLab** rozwiązuje ten problem poprzez uporządkowanie procesu rezerwacji. Trener może zarządzać swoim grafikiem, a klient widzi tylko dostępne terminy i może samodzielnie dokonać rezerwacji.

---

## 👥 Główne role w systemie

### 🧍 Klient

Klient może:

- zarejestrować konto,
- zalogować się do aplikacji,
- przeglądać dostępnych trenerów,
- sprawdzać wolne terminy treningowe,
- rezerwować treningi,
- odwoływać własne rezerwacje,
- przeglądać historię swoich rezerwacji,
- zarządzać swoim profilem.

### 🏋️ Trener

Trener może:

- zarejestrować konto trenera,
- zalogować się do aplikacji,
- tworzyć dostępne terminy treningowe,
- zarządzać swoim grafikiem,
- przeglądać rezerwacje klientów,
- anulować własne terminy,
- oznaczać treningi jako zakończone,
- zarządzać swoim profilem i specjalizacją.

---

## ✨ Najważniejsze funkcjonalności

- rejestracja klienta i trenera,
- logowanie użytkowników,
- podział uprawnień na role `CLIENT` oraz `COACH`,
- zabezpieczenie endpointów za pomocą Spring Security,
- szyfrowanie haseł z użyciem BCrypt,
- tworzenie dostępnych terminów treningowych przez trenera,
- przeglądanie dostępnych terminów przez klienta,
- rezerwowanie treningów,
- anulowanie rezerwacji,
- oznaczanie treningów jako zakończone,
- obsługa statusów treningów i rezerwacji,
- filtrowanie, sortowanie i paginacja danych,
- walidacja danych wejściowych,
- globalna obsługa błędów,
- zwracanie czytelnych komunikatów błędów w formacie JSON,
- testy jednostkowe logiki biznesowej.

---

## 🧱 Architektura aplikacji

Projekt został wykonany w architekturze warstwowej.

Główne warstwy aplikacji:

- **Controller** - obsługa żądań HTTP,
- **Service** - logika biznesowa aplikacji,
- **Repository** - komunikacja z bazą danych,
- **Model** - encje bazodanowe,
- **DTO** - obiekty używane do przesyłania danych,
- **Mapper** - mapowanie encji na DTO oraz DTO na encje,
- **Exception** - własne wyjątki i globalna obsługa błędów,
- **Security** - konfiguracja bezpieczeństwa aplikacji.

Taki podział ułatwia rozwijanie aplikacji, testowanie logiki biznesowej oraz utrzymanie czytelnej struktury projektu.

---

## 🛠️ Technologie

### Backend

- Java 21
- Spring Boot 3.5.15
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- Maven
- H2 Database
- MapStruct
- Lombok

### Frontend

- HTML
- CSS
- JavaScript

### Testy

- JUnit 5
- Mockito

### Narzędzia

- IntelliJ IDEA
- Swagger / OpenAPI
- pliki `.http`
- Git / GitHub

---

## 🧠 Logika biznesowa

Aplikacja posiada reguły biznesowe zabezpieczające poprawność działania systemu.

Przykładowe reguły:

- klient może rezerwować tylko dostępne terminy,
- trener może zarządzać tylko własnymi terminami,
- użytkownik może wykonywać tylko akcje zgodne ze swoją rolą,
- nie można rezerwować terminu z przeszłości,
- nie można anulować treningu zbyt krótko przed jego rozpoczęciem,
- klient nie może przekroczyć limitu 3 rezerwacji jednego dnia,
- klient nie może posiadać więcej niż 10 aktywnych rezerwacji w przyszłości,
- status treningu kontroluje możliwe operacje,
- zakończonego treningu nie można anulować.

---

## 📌 Statusy w systemie

### Statusy terminu treningowego

- `AVAILABLE` - termin dostępny do rezerwacji,
- `BOOKED` - termin zarezerwowany,
- `COMPLETED` - trening zakończony,
- `CANCELLED` - trening anulowany,
- `MISSED` - trening pominięty.

### Statusy rezerwacji

- `BOOKED` - rezerwacja aktywna,
- `COMPLETED` - rezerwacja zakończona,
- `CANCELLED` - rezerwacja anulowana.

---

## 🔐 Bezpieczeństwo

Aplikacja wykorzystuje Spring Security do kontroli dostępu.

Zastosowane mechanizmy:

- logowanie użytkowników,
- Basic Auth,
- szyfrowanie haseł za pomocą BCrypt,
- podział użytkowników na role `CLIENT` oraz `COACH`,
- ograniczenie dostępu do endpointów na podstawie roli,
- ochrona przed wykonywaniem akcji na cudzych zasobach,
- globalna obsługa wyjątków bezpieczeństwa,
- zwracanie odpowiednich kodów HTTP, między innymi `401` i `403`.

---

## ✅ Walidacja i obsługa błędów

System waliduje dane wejściowe oraz obsługuje błędy w ujednolicony sposób.

W projekcie wykorzystano:

- Bean Validation,
- własne wyjątki biznesowe,
- globalny handler wyjątków,
- czytelne odpowiedzi błędów w formacie JSON,
- odpowiednie statusy HTTP.

Przykładowe obsługiwane sytuacje:

- niepoprawne dane rejestracji,
- błędny e-mail,
- zbyt krótkie lub puste pola,
- brak użytkownika,
- brak terminu treningowego,
- brak rezerwacji,
- niepoprawna rola użytkownika,
- niepoprawny status treningu,
- próba wykonania akcji na cudzym zasobie,
- przekroczenie limitu rezerwacji.

---

## 🧪 Testowanie

Projekt został przetestowany manualnie oraz automatycznie.

Zakres testów:

- testy manualne endpointów,
- testy z użyciem plików `.http`,
- testy walidacji danych,
- testy obsługi błędów,
- testy jednostkowe warstwy Service,
- testy happy path oraz fail path,
- testy logiki ról i statusów,
- testy limitów rezerwacji.

W testach jednostkowych wykorzystano **JUnit 5** oraz **Mockito**. Testowana była przede wszystkim warstwa serwisowa, ponieważ to tam znajduje się główna logika biznesowa aplikacji.

---

## 📦 Przykładowe moduły aplikacji

### UserService

Odpowiada za:

- rejestrację użytkowników,
- logowanie,
- zmianę danych profilu,
- zmianę hasła,
- zmianę e-maila,
- zmianę nazwy użytkownika,
- usuwanie konta,
- kontrolę ról.

### TrainingSlotService

Odpowiada za:

- tworzenie dostępnych terminów przez trenera,
- anulowanie terminów,
- zmianę czasu treningu,
- sprawdzanie właściciela terminu,
- kontrolę statusu terminu,
- wyszukiwanie dostępnych treningów.

### ReservationService

Odpowiada za:

- tworzenie rezerwacji przez klienta,
- anulowanie rezerwacji przez klienta,
- anulowanie rezerwacji przez trenera,
- oznaczanie treningu jako zakończony,
- kontrolę limitów rezerwacji,
- kontrolę statusów,
- sprawdzanie poprawności operacji biznesowych.

---

## 🚀 Możliwe dalsze rozszerzenia

Projekt można rozwinąć o:

- JWT zamiast Basic Auth,
- panel administratora,
- powiadomienia e-mail,
- płatności online,
- statystyki treningowe,
- kalendarz treningów,
- integrację z zewnętrznym systemem płatności,
- bardziej rozbudowany frontend,
- wdrożenie aplikacji na serwer.

---

## 👨‍💻 Autor

**Kacper Ciepieńka**  

---

## 📚 Podsumowanie

**MuscleLab** to aplikacja pokazująca pełny przepływ pracy backendowej w Spring Boot: użytkownicy, role, bezpieczeństwo, rezerwacje, statusy, walidacja, DTO, obsługa błędów oraz testy jednostkowe.

Projekt stanowi praktyczny przykład systemu rezerwacji treningów personalnych i może być dalej rozwijany o kolejne funkcjonalności.
