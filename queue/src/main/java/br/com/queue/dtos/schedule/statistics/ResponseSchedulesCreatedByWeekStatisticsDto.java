package br.com.queue.dtos.schedule.statistics;

public record ResponseSchedulesCreatedByWeekStatisticsDto(
        Integer day,
        String weekDay,
        Long totalSchedules
) {
}
