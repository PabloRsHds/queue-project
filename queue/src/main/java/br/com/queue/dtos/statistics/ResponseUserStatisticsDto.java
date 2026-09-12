package br.com.queue.dtos.statistics;

import java.math.BigDecimal;

public record ResponseUserStatisticsDto(
        Long admins,
        Long totalElements,
        Long totalElementsActive,
        Long totalElementsInactive,
        BigDecimal percentageActive,
        BigDecimal percentageInactive
) {
}
