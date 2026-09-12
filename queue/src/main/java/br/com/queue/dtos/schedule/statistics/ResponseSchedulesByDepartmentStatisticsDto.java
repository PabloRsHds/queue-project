package br.com.queue.dtos.schedule.statistics;

import java.math.BigDecimal;

public record ResponseSchedulesByDepartmentStatisticsDto(
        String departmentName,
        Long totalSchedules,
        BigDecimal percentage
) {
}
