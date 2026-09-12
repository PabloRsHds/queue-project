package br.com.queue.dtos.attendance.statistics;

import java.math.BigDecimal;

public record ResponseAttendancesByDepartmentStatisticsDto(
        String departmentName,
        Long totalAttendances,
        BigDecimal percentage
) {
}
