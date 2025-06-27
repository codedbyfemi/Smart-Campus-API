package com.example.lecturer_availability_module.IService;

import com.example.lecturer_availability_module.DTO.LecturerDTO;
import com.example.lecturer_availability_module.DTO.ScheduleDTO;
import com.example.lecturer_availability_module.Entity.LecturerEntity;
import com.example.lecturer_availability_module.Entity.ScheduleEntry;
import com.example.lecturer_availability_module.LecturerRepo;
import com.example.lecturer_availability_module.ScheduleRepo;
import jakarta.inject.Singleton;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class LecturerServiceImpl implements ILecturerService{
    private final LecturerRepo lecturerRepo;
    private final ScheduleRepo scheduleRepo;

    public LecturerServiceImpl(LecturerRepo lecturerRepo, ScheduleRepo scheduleRepo) {
        this.lecturerRepo = lecturerRepo;
        this.scheduleRepo = scheduleRepo;
    }

    @Override
    public LecturerDTO createLecturer(LecturerDTO dto) {
        // 1. Create the Lecturer
        LecturerEntity lecturer = new LecturerEntity();
        lecturer.setName(dto.name);
        lecturer.setDepartment(dto.department);
        lecturer.setEmail(dto.email);

        // 2. For each schedule, create ScheduleEntry and link to lecturer
        for (ScheduleDTO s : dto.schedule) {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setDay(s.day);
            entry.setStartTime(LocalTime.parse(s.startTime)); // e.g., "14:00"
            entry.setEndTime(LocalTime.parse(s.endTime));
            entry.setLecturer(lecturer); // link to lecturer

            lecturer.getSchedule().add(entry); // add to lecturer's list
        }

        // 3. Save the lecturer (and cascade saves schedule)
        LecturerEntity saved = lecturerRepo.save(lecturer);

        // 4. Return the saved Lecturer as DTO
        LecturerDTO result = new LecturerDTO();
        result.name = saved.getName();
        result.department = saved.getDepartment();
        result.email = saved.getEmail();

        result.schedule = saved.getSchedule().stream().map(entry -> new ScheduleDTO(
                entry.getDay(),
                entry.getStartTime().toString(),
                entry.getEndTime().toString(),
                false, // isAvailableNow — not relevant at creation
                null,  // nextAvailableInMinutes
                null   // nextAvailableAt
        )).collect(Collectors.toList());

        return result;
    }

    @Override
    public List<LecturerDTO> getAllLecturers() {
        List<LecturerEntity> lecturers = lecturerRepo.findAllWithSchedule();

        LocalTime now = LocalTime.now(ZoneId.of("Africa/Lagos"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Africa/Lagos")).getDayOfWeek();

        return lecturers.stream().map(l -> {
            LecturerDTO dto = new LecturerDTO();
            dto.name = l.getName();
            dto.department = l.getDepartment();
            dto.officeBuilding = l.getOfficeBuilding();
            dto.officeNumber = l.getOfficeNumber();
            dto.email = l.getEmail();

            dto.schedule = l.getSchedule().stream().map(s -> {
                boolean isToday = s.getDay().equalsIgnoreCase(today.name());
                LocalTime start = s.getStartTime();
                LocalTime end = s.getEndTime();

                boolean isNow = isToday && !now.isBefore(start) && !now.isAfter(end);
                Long mins = null;
                String nextAt = null;

                if (isToday && now.isBefore(start)) {
                    mins = Duration.between(now, start).toMinutes();
                    nextAt = start.toString();
                }

                return new ScheduleDTO(
                        s.getDay(),
                        start.toString(),
                        end.toString(),
                        isNow,
                        mins,
                        nextAt
                );
            }).collect(Collectors.toList());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public LecturerDTO getLecturerByName(String name) {
        Optional<LecturerEntity> optional = lecturerRepo.findByNameIgnoreCase(name);

        if (optional.isEmpty()) {
            throw new RuntimeException("Lecturer not found with name: " + name);
        }

        LecturerEntity lecturerEntity = optional.get();
        LecturerDTO lecturerDTO = new LecturerDTO();
        lecturerDTO.setName(lecturerEntity.getName());
        lecturerDTO.setDepartment(lecturerEntity.getDepartment());
        lecturerDTO.setEmail(lecturerEntity.getEmail());
        lecturerDTO.setOfficeBuilding(lecturerEntity.getOfficeBuilding());
        lecturerDTO.setOfficeNumber(lecturerEntity.getOfficeNumber());

        LocalTime now = LocalTime.now(ZoneId.of("Africa/Lagos"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Africa/Lagos")).getDayOfWeek();

        lecturerDTO.schedule = lecturerEntity.getSchedule().stream().map(s -> {
            boolean isToday = s.getDay().equalsIgnoreCase(today.name());
            LocalTime start = s.getStartTime();
            LocalTime end = s.getEndTime();

            boolean isNow = isToday && !now.isBefore(start) && !now.isAfter(end);
            Long mins = null;
            String nextAt = null;

            if (isToday && now.isBefore(start)) {
                mins = Duration.between(now, start).toMinutes();
                nextAt = start.toString();
            }

            return new ScheduleDTO(
                    s.getDay(),
                    start.toString(),
                    end.toString(),
                    isNow,
                    mins,
                    nextAt
            );
        }).collect(Collectors.toList());

        return lecturerDTO;
    }

    @Override
    public List<LecturerDTO> getAvailableLecturers(DayOfWeek dayhe, LocalTime time) {
        String day = dayhe.name();
        List<LecturerEntity> availableLecturers = scheduleRepo.findAvailableLecturers(day, time);
        return availableLecturers.stream().map(l -> {
            LecturerDTO dto = new LecturerDTO();
            dto.name = l.getName();
            dto.department = l.getDepartment();
            dto.officeBuilding = l.getOfficeBuilding();
            dto.officeNumber = l.getOfficeNumber();
            dto.email = l.getEmail();

            dto.schedule = l.getSchedule().stream().map(s -> {
                boolean isToday = s.getDay().equalsIgnoreCase(day);
                LocalTime start = s.getStartTime();
                LocalTime end = s.getEndTime();

                boolean isNow = isToday && !time.isBefore(start) && !time.isAfter(end);
                Long mins = null;
                String nextAt = null;

                if (isToday && time.isBefore(start)) {
                    mins = Duration.between(time, start).toMinutes();
                    nextAt = start.toString();
                }

                return new ScheduleDTO(
                        s.getDay(),
                        start.toString(),
                        end.toString(),
                        isNow,
                        mins,
                        nextAt
                );
            }).collect(Collectors.toList());

            return dto;
        }).collect(Collectors.toList());
    }
}
