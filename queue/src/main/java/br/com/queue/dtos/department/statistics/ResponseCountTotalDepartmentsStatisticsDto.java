package br.com.queue.dtos.department.statistics;

public record ResponseCountTotalDepartmentsStatisticsDto(
        Long totalElements,
        Long totalElementsActive,
        Long totalElementsInactive
) {
}
