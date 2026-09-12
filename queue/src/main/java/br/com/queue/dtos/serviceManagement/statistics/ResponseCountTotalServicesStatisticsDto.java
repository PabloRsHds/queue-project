package br.com.queue.dtos.serviceManagement.statistics;

public record ResponseCountTotalServicesStatisticsDto(
        Long totalElements,
        Long totalElementsActive,
        Long totalElementsInactive
) {
}
