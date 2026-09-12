package br.com.queue.dtos.schedule.statistics;

public record ResponseCountTotalSchedulesStatisticsDto(

        Long totalElements,
        Long scheduled,
        Long present,
        Long canceled,
        Long absent

) {
}
