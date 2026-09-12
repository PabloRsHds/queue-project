package br.com.queue.dtos.attendance.statistics;

import java.math.BigDecimal;

public record ResponseAttendancesByServiceStatisticsDto(
        String serviceName,
        Long totalAttendances,
        BigDecimal percentage
) {
}
