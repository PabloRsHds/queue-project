package br.com.queue.dtos.schedule.statistics;

public record ResponseSchedulesByHourStatisticsDto(
        Integer hour,
        Long totalSchedules
) {
}
