package br.com.queue.dtos.attendance.statistics;

public record ResponseAttendancesByWeekStatisticsDto(
        Integer dayOfWeek,
        String dayName,
        Long totalAttendances
) {
}
