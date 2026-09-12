package br.com.queue.dtos.attendance.statistics;

public record ResponseAverageAttendanceByUserStatisticsDto(
        String username,
        String  averageMinutes
) {
}
