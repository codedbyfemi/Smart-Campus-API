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

/**
 * Implementation of the ILecturerService interface.
 * Handles core logic related to managing Lecturer entities and their availability schedules.
 */
@Singleton
public class LecturerServiceImpl implements ILecturerService {

    private final LecturerRepo lecturerRepo;
    private final ScheduleRepo scheduleRepo;

    /**
     * Constructor for injecting the required repositories.
     *
     * @param lecturerRepo Repository for LecturerEntity
     * @param scheduleRepo Repository for ScheduleEntry
     */
    public LecturerServiceImpl(LecturerRepo lecturerRepo, ScheduleRepo scheduleRepo) {
        this.lecturerRepo = lecturerRepo;
        this.scheduleRepo = scheduleRepo;
    }

    /**
     * Creates a new lecturer and their associated schedule.
     *
     * @param dto LecturerDTO containing lecturer and schedule data
     * @return LecturerDTO with saved values
     */
    @Override
    public LecturerDTO createLecturer(LecturerDTO dto) {
        // Create new LecturerEntity from DTO
        LecturerEntity lecturer = new LecturerEntity();
        lecturer.setName(dto.name);
        lecturer.setDepartment(dto.department);
        lecturer.setEmail(dto.email);
        lecturer.setOfficeBuilding(dto.officeBuilding);
        lecturer.setOfficeNumber(dto.officeNumber);

        // Populate ScheduleEntry list from ScheduleDTOs
        for (ScheduleDTO s : dto.schedule) {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setDay(s.day);
            entry.setStartTime(LocalTime.parse(s.startTime)); // Converts "14:00" string to LocalTime
            entry.setEndTime(LocalTime.parse(s.endTime));     // Same as above
            entry.setLecturer(lecturer); // Establishes bi-directional association

            lecturer.getSchedule().add(entry); // Adds to list held by lecturer
        }

        // Save lecturer and cascade to save schedules
        LecturerEntity saved = lecturerRepo.save(lecturer); // `save()` persists the entity and cascades to schedule due to JPA config

        // Map back to DTO (conversion from Entity to DTO)
        LecturerDTO result = new LecturerDTO();
        result.name = saved.getName();
        result.department = saved.getDepartment();
        result.email = saved.getEmail();
        result.officeBuilding = saved.getOfficeBuilding();
        result.officeNumber = saved.getOfficeNumber();

        // Use Java Stream API to convert list of ScheduleEntry to ScheduleDTO
        result.schedule = saved.getSchedule().stream().map(entry -> new ScheduleDTO(
                entry.getDay(),
                entry.getStartTime().toString(),
                entry.getEndTime().toString(),
                false,
                null,
                null
        )).collect(Collectors.toList()); // Collects results into a new list

        return result;
    }

    /**
     * Adds a schedule to an existing lecturer.
     *
     * @param name            Name of the lecturer
     * @param newScheduleDTO  Schedule data to be added
     * @return Updated LecturerDTO
     */
    @Override
    public LecturerDTO addScheduleToLecturer(String name, ScheduleDTO newScheduleDTO) {
        // Find lecturer by name, ignoring case
        LecturerEntity lecturer = lecturerRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + name));
        // `Optional.orElseThrow()` throws an exception if lecturer is not found

        // Create ScheduleEntry from DTO
        ScheduleEntry entry = new ScheduleEntry();
        entry.setDay(newScheduleDTO.getDay());
        entry.setStartTime(LocalTime.parse(newScheduleDTO.getStartTime()));
        entry.setEndTime(LocalTime.parse(newScheduleDTO.getEndTime()));
        entry.setLecturer(lecturer);

        lecturer.getSchedule().add(entry); // Append to lecturer's current schedule list

        LecturerEntity updated = lecturerRepo.update(lecturer); // Save updated lecturer

        // Map updated entity to DTO
        LecturerDTO dto = new LecturerDTO();
        dto.setName(updated.getName());
        dto.setDepartment(updated.getDepartment());
        dto.setOfficeBuilding(updated.getOfficeBuilding());
        dto.setOfficeNumber(updated.getOfficeNumber());
        dto.setEmail(updated.getEmail());

        // Convert updated schedule entries to DTOs
        dto.setSchedule(updated.getSchedule().stream().map(s -> new ScheduleDTO(
                s.getDay(),
                s.getStartTime().toString(),
                s.getEndTime().toString(),
                false,
                null,
                null
        )).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Retrieves a list of all lecturers with schedule and availability status.
     *
     * @return List of LecturerDTO
     */
    @Override
    public List<LecturerDTO> getAllLecturers() {
        List<LecturerEntity> lecturers = lecturerRepo.findAllWithSchedule(); // Custom query with join fetch

        // Get current day and time in Africa/Lagos timezone
        LocalTime now = LocalTime.now(ZoneId.of("Africa/Lagos"));
        DayOfWeek today = LocalDate.now(ZoneId.of("Africa/Lagos")).getDayOfWeek();

        return lecturers.stream().map(l -> {
            LecturerDTO dto = new LecturerDTO();
            dto.name = l.getName();
            dto.department = l.getDepartment();
            dto.officeBuilding = l.getOfficeBuilding();
            dto.officeNumber = l.getOfficeNumber();
            dto.email = l.getEmail();

            // Map each ScheduleEntry to ScheduleDTO with real-time availability info
            dto.schedule = l.getSchedule().stream().map(s -> {
                boolean isToday = s.getDay().equalsIgnoreCase(today.name());
                LocalTime start = s.getStartTime();
                LocalTime end = s.getEndTime();

                boolean isNow = isToday && !now.isBefore(start) && !now.isAfter(end); // Checks if current time is within bounds

                Long mins = null;
                String nextAt = null;

                // If not available now but available later today
                if (isToday && now.isBefore(start)) {
                    mins = Duration.between(now, start).toMinutes(); // Calculates time until availability
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

    /**
     * Retrieves a lecturer by name.
     *
     * @param name Lecturer's name
     * @return LecturerDTO
     */
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

    /**
     * Returns list of lecturers available at a given day and time.
     *
     * @param dayhe  Day of the week
     * @param time LocalTime value
     * @return List of LecturerDTO
     */
    @Override
    public List<LecturerDTO> getAvailableLecturers(DayOfWeek dayhe, LocalTime time) {
        String day = dayhe.name(); // Get string representation of enum
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

    /**
     * Removes a specific schedule entry from a lecturer's record.
     *
     * @param name         Lecturer's name
     * @param toRemoveDTO  Schedule entry to be removed
     * @return Updated LecturerDTO
     */
    @Override
    public LecturerDTO removeScheduleFromLecturer(String name, ScheduleDTO toRemoveDTO) {
        LecturerEntity lecturer = lecturerRepo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Lecturer not found: " + name));

        // Filters out the matching schedule entry
        List<ScheduleEntry> remaining = lecturer.getSchedule().stream()
                .filter(s -> !(s.getDay().equalsIgnoreCase(toRemoveDTO.getDay()) &&
                        s.getStartTime().equals(LocalTime.parse(toRemoveDTO.getStartTime())) &&
                        s.getEndTime().equals(LocalTime.parse(toRemoveDTO.getEndTime()))))
                .collect(Collectors.toList());

        lecturer.setSchedule(remaining); // Replace with filtered list
        LecturerEntity updated = lecturerRepo.update(lecturer); // Persist changes

        LecturerDTO dto = new LecturerDTO();
        dto.setName(updated.getName());
        dto.setDepartment(updated.getDepartment());
        dto.setOfficeBuilding(updated.getOfficeBuilding());
        dto.setOfficeNumber(updated.getOfficeNumber());
        dto.setEmail(updated.getEmail());

        dto.setSchedule(updated.getSchedule().stream().map(s -> new ScheduleDTO(
                s.getDay(),
                s.getStartTime().toString(),
                s.getEndTime().toString(),
                false,
                null,
                null
        )).collect(Collectors.toList()));

        return dto;
    }
}
