package br.com.queue.dtos.department.statistics;

import java.math.BigDecimal;

public record ResponseDepartmentPercentagesStatisticsDto(
        BigDecimal percentageActive,
        BigDecimal percentageInactive
) {
}
