package br.com.queue.dtos.attendance.statistics;

import java.math.BigDecimal;

public record ResponseAttendancesByCustomerStatisticsDto(
        String username,
        Long totalAttendances,
        BigDecimal percentage
) {
}
