package pl.kacper.musclelab.mapper.create;

import org.mapstruct.Mapper;
import pl.kacper.musclelab.dto.create.CreateReservation;
import pl.kacper.musclelab.model.Reservation;

@Mapper(componentModel = "spring")
public interface CreateReservationMapper {
    Reservation toEntity(CreateReservation reservation);
}
