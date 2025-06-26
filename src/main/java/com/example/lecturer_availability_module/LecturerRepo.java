package com.example.lecturer_availability_module;

import com.example.lecturer_availability_module.Entity.LecturerEntity;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerRepo extends CrudRepository<LecturerEntity, Long> {

    @Query("SELECT l FROM LecturerEntity l LEFT JOIN FETCH l.schedule")
    List<LecturerEntity> findAllWithSchedule();

    @Query("SELECT l FROM LecturerEntity l LEFT JOIN FETCH l.schedule WHERE LOWER(l.name) = LOWER(:name)")
    Optional<LecturerEntity> findByNameIgnoreCase(String name);
}
