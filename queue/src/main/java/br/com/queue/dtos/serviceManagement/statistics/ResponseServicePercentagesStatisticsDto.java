package br.com.queue.dtos.serviceManagement.statistics;

import java.math.BigDecimal;

public record ResponseServicePercentagesStatisticsDto(
        BigDecimal percentageActive,
        BigDecimal percentageInactive
) {
}
