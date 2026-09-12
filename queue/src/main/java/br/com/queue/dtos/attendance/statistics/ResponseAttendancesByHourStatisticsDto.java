package br.com.queue.dtos.attendance.statistics;

public record ResponseAttendancesByHourStatisticsDto(
        Integer hour,
        Long totalAttendances
) {
}
