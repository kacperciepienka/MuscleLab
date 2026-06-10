package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kacper.musclelab.dto.entity.TrainingSlotClientDTO;

import pl.kacper.musclelab.model.TrainingSlot;

@Mapper(componentModel = "spring")
public interface TrainingSlotClientDTOMapper {
    @Mapping(target = "coachUsername", source = "coach.username")
    @Mapping(target = "coachEmail", source = "coach.email")
    @Mapping(target = "coachFirstName", source = "coach.firstName")
    @Mapping(target = "coachSpecialisation", source = "coach.specialisation")
    TrainingSlotClientDTO toDto(TrainingSlot slot);
}
