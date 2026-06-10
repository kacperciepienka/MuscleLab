package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kacper.musclelab.dto.entity.ReservationClientDTO;
import pl.kacper.musclelab.model.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationClientDTOMapper {
    @Mapping(target = "reservationClientUsername", source = "client.username")
    @Mapping(target = "reservationClientEmail", source = "client.email")
    @Mapping(target = "reservationClientFirstName", source = "client.firstName")

    @Mapping(target = "reservationTrainingSlotCode", source = "trainingSlot.slotCode")
    @Mapping(target = "reservationTrainingSlotCoachUsername", source = "trainingSlot.coach.username")
    @Mapping(target = "reservationTrainingSlotCoachEmail", source = "trainingSlot.coach.email")
    @Mapping(target = "reservationTrainingSlotCoachFirstName", source = "trainingSlot.coach.firstName")
    @Mapping(target = "reservationTrainingSlotCoachExperience", source = "trainingSlot.coach.experience")
    @Mapping(target = "reservationTrainingSlotCoachSpecialisation", source = "trainingSlot.coach.specialisation")
    @Mapping(target = "reservationTrainingSlotStartTime", source = "trainingSlot.startTime")
    @Mapping(target = "reservationTrainingSlotEndTime", source = "trainingSlot.endTime")
    ReservationClientDTO toDto(Reservation reservation);
}
