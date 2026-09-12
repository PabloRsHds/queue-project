package br.com.queue.dtos.statistics;

import java.math.BigDecimal;

public record ResponseStatisticsDto(
        Long totalElements,
        Long totalElementsActive,
        Long totalElementsInactive,
        BigDecimal percentageActive,
        BigDecimal percentageInactive
) {
}
