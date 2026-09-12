package br.com.queue.dtos.serviceManagement.statistics;

import java.math.BigDecimal;

public record ResponseServicesByDepartmentStatisticsDto(
        String departmentName,
        Long totalServices,
        BigDecimal percentage
) {
}
