package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.entity.CoachPublicDTO;
import pl.kacper.musclelab.model.User;

@Mapper(componentModel = "spring")
public interface CoachPublicDTOMapper {
    CoachPublicDTO toDto(User user);
}
