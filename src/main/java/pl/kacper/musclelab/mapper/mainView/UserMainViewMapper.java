package pl.kacper.musclelab.mapper.mainView;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.mainView.UserMainViewDTO;
import pl.kacper.musclelab.model.User;

@Mapper(componentModel = "spring")
public interface UserMainViewMapper {
    UserMainViewDTO toDto(User client);
}
