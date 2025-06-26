package com.example.lecturer_availability_module.IService;

import com.example.lecturer_availability_module.DTO.LecturerDTO;

import java.util.List;

public interface ILecturerService {

    // 1. Create a lecturer with schedule
    LecturerDTO createLecturer(LecturerDTO dto);

    // 2. Get all lecturers (each with schedule and availability info)
    List<LecturerDTO> getAllLecturers();

    // 3. Get one lecturer by name(with schedule and availability info)
    LecturerDTO getLecturerByName(String name);

}
