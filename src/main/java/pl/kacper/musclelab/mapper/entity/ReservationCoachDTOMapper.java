package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kacper.musclelab.dto.entity.ReservationCoachDTO;
import pl.kacper.musclelab.model.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationCoachDTOMapper {
    @Mapping(target = "reservationClientUsername", source = "client.username")
    @Mapping(target = "reservationClientEmail", source = "client.email")
    @Mapping(target = "reservationClientFirstName", source = "client.firstName")
    @Mapping(target = "reservationClientExperience", source = "client.experience")

    @Mapping(target = "reservationTrainingSlotCode", source = "trainingSlot.slotCode")
    @Mapping(target = "reservationTrainingSlotCoachUsername", source = "trainingSlot.coach.username")
    @Mapping(target = "reservationTrainingSlotCoachFirstName", source = "trainingSlot.coach.firstName")
    @Mapping(target = "reservationTrainingSlotStartTime", source = "trainingSlot.startTime")
    @Mapping(target = "reservationTrainingSlotEndTime", source = "trainingSlot.endTime")
    ReservationCoachDTO toDto(Reservation reservation);
}
