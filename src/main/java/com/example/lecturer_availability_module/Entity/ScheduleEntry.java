package com.example.lecturer_availability_module.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String day; // "MONDAY", "TUESDAY", etc.

    private LocalTime startTime;

    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private LecturerEntity lecturer;


}
