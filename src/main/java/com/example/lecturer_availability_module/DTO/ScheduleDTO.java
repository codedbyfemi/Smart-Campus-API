package com.example.lecturer_availability_module.DTO;

import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    public String day;
    public String startTime;
    public String endTime;
    public boolean isAvailableNow;
    public Long nextAvailableInMinutes;
    public String nextAvailableAt;
}
