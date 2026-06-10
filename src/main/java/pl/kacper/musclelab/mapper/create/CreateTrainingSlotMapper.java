package pl.kacper.musclelab.mapper.create;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.kacper.musclelab.dto.create.CreateTrainingSlot;
import pl.kacper.musclelab.model.TrainingSlot;

@Mapper(componentModel = "spring")
public interface CreateTrainingSlotMapper {
    TrainingSlot toEntity(CreateTrainingSlot slot);
}
