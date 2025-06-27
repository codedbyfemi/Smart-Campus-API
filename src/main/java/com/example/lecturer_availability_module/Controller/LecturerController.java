package com.example.lecturer_availability_module.Controller;

import com.example.lecturer_availability_module.DTO.LecturerDTO;
import com.example.lecturer_availability_module.IService.ILecturerService;
import io.micronaut.http.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Controller("/lecturers")
public class LecturerController {

    private final ILecturerService lecturerService;

    public LecturerController(ILecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    /**
     * Create a new lecturer with a schedule
     */
    @Post
    public LecturerDTO createLecturer(@Body LecturerDTO dto) {
        return lecturerService.createLecturer(dto);
    }

    /**
     * Get all lecturers and their availability
     */
    @Get
    public List<LecturerDTO> getAllLecturers() {
        return lecturerService.getAllLecturers();
    }

    /**
     * Get a specific lecturer by name
     */
    @Get("/{name}")
    public LecturerDTO getLecturerByName(@PathVariable String name) {
        return lecturerService.getLecturerByName(name);
    }

    /**
     * Get all available lecturers
     */
    @Get("/available")
    public List<LecturerDTO> getAvailableLecturers(@QueryValue DayOfWeek day, @QueryValue LocalTime time) {
        return lecturerService.getAvailableLecturers(day, time);
    }

}
