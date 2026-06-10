package pl.kacper.musclelab.mapper.entity;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.entity.UserCoachDTO;
import pl.kacper.musclelab.model.User;

@Mapper(componentModel = "spring")
public interface UserCoachDTOMapper {
    UserCoachDTO toDto(User user);
}
