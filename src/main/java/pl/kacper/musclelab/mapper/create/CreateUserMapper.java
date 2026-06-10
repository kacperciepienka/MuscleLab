package pl.kacper.musclelab.mapper.create;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.create.CreateUser;
import pl.kacper.musclelab.model.User;

@Mapper(componentModel = "spring")
public interface CreateUserMapper {
    User toEntity(CreateUser user);
}
