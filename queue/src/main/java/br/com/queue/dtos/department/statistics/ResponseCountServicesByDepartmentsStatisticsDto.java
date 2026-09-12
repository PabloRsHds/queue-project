package br.com.queue.dtos.department.statistics;

import java.math.BigDecimal;

public record ResponseCountServicesByDepartmentsStatisticsDto(
        String departmentName,
        Long totalServices,
        BigDecimal percentage
) {
}
