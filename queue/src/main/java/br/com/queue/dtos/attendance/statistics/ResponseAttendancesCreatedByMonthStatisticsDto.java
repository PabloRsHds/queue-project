package br.com.queue.dtos.attendance.statistics;

public record ResponseAttendancesCreatedByMonthStatisticsDto(
        Integer month,
        String monthName,
        Long totalAttendances
) {
}
