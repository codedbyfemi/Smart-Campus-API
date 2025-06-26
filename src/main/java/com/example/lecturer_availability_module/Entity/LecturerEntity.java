package com.example.lecturer_availability_module.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LecturerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String department;
    private String email;
    private String officeBuilding;
    private String officeNumber;

    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEntry> schedule = new ArrayList<>();
}
