package br.com.queue.dtos.serviceManagement.statistics;

import java.math.BigDecimal;

public record ResponseSchedulesByServiceStatisticsDto(
        String serviceName,
        Long totalSchedules,
        BigDecimal percentage
) {
}
