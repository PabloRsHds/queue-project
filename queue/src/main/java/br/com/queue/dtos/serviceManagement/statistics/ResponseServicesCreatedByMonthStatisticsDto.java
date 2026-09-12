package br.com.queue.dtos.serviceManagement.statistics;

public record ResponseServicesCreatedByMonthStatisticsDto(
        Integer month,
        String monthName,
        Long totalServices
) {
}
