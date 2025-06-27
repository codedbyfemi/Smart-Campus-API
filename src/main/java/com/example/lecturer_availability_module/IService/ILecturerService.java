package com.example.lecturer_availability_module.IService;

import com.example.lecturer_availability_module.DTO.LecturerDTO;
import com.example.lecturer_availability_module.DTO.ScheduleDTO;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface ILecturerService {

    // 1. Create a lecturer with schedule
    LecturerDTO createLecturer(LecturerDTO dto);

    LecturerDTO addScheduleToLecturer(String name, ScheduleDTO newScheduleDTO);

    // 2. Get all lecturers (each with schedule and availability info)
    List<LecturerDTO> getAllLecturers();

    // 3. Get one lecturer by name(with schedule and availability info)
    LecturerDTO getLecturerByName(String name);


    // 4. Get available lecturers by date and time(with schedule and availability info)
    List<LecturerDTO> getAvailableLecturers(DayOfWeek day, LocalTime time);

    LecturerDTO removeScheduleFromLecturer(String name, ScheduleDTO toRemoveDTO);
}
