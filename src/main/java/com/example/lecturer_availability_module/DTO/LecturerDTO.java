package com.example.lecturer_availability_module.DTO;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDTO {
    public String name;
    public String department;
    public String officeBuilding;
    public String officeNumber;
    public String email;
    public List<ScheduleDTO> schedule;

}
