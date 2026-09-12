package br.com.queue.dtos.schedule.statistics;

public record ResponseSchedulesCreatedByDayStatisticsDto(

        long absentCustomer,
        long customerPresent,
        long schedulingCanceled,
        long schedulingOfDay

) {
}
