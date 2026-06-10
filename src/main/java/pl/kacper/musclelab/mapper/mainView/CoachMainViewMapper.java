package pl.kacper.musclelab.mapper.mainView;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.mainView.CoachMainViewDTO;
import pl.kacper.musclelab.model.User;

@Mapper(componentModel = "spring")
public interface CoachMainViewMapper {
    CoachMainViewDTO toDto(User coach);
}
