package com.example.lecturer_availability_module;

import com.example.lecturer_availability_module.Entity.LecturerEntity;
import com.example.lecturer_availability_module.Entity.ScheduleEntry;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepo extends CrudRepository<ScheduleEntry, Long> {

    @Query("SELECT s.lecturer FROM ScheduleEntry s WHERE s.day = :day AND s.startTime <= :time AND s.endTime >= :time")
    List<LecturerEntity> findAvailableLecturers(String day, LocalTime time);

}
