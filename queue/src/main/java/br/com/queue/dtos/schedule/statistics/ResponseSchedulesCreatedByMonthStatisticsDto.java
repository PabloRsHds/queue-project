package br.com.queue.dtos.schedule.statistics;

public record ResponseSchedulesCreatedByMonthStatisticsDto(
        Integer month,
        String monthName,
        Long totalSchedules
) {
}
