package br.com.queue.dtos.schedule.statistics;

import java.math.BigDecimal;

public record ResponseSchedulesByPriorityStatisticsDto(
        String priority,
        Long totalSchedules,
        BigDecimal percentage
) {
}
