package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kacper.musclelab.dto.entity.TrainingSlotCoachDTO;
import pl.kacper.musclelab.model.TrainingSlot;

@Mapper(componentModel = "spring")
public interface TrainingSlotCoachDTOMapper {
    @Mapping(target = "coachUsername", source = "coach.username")
    @Mapping(target = "coachFirstName", source = "coach.firstName")
    TrainingSlotCoachDTO toDto(TrainingSlot slot);
}
