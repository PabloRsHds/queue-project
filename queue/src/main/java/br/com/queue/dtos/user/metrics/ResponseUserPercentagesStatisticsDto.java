package br.com.queue.dtos.user.metrics;

import java.math.BigDecimal;

public record ResponseUserPercentagesStatisticsDto(
        BigDecimal percentageActive,
        BigDecimal percentageInactive
) {
}
