package br.com.queue.dtos.schedule.statistics;

import java.math.BigDecimal;

public record ResponseSchedulePercentagesStatisticsDto(

        BigDecimal scheduled,
        BigDecimal present,
        BigDecimal canceled,
        BigDecimal absent

) {
}
