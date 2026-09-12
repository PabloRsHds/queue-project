package br.com.queue.dtos.attendance.statistics;

public record ResponseCountTotalAttendancesStatisticsDto(
        long countAttendancesWaiting,
        long countAttendancesInProgress,
        long attendancesToday,
        long attendancesThisWeek,
        long attendancesThisMonth,
        long totalAttendances
) {
}
