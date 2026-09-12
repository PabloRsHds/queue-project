package br.com.queue.dtos.serviceManagement.statistics;

import java.math.BigDecimal;

public record ResponseUsersByServiceStatisticsDto(
        String serviceName,
        Long totalUsers,
        BigDecimal percentage
) {
}
