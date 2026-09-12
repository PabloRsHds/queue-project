package br.com.queue.dtos.user.metrics;

public record ResponseCountTotalUsersStatisticsDto(
        Long totalElements,
        Long totalElementsActive,
        Long totalElementsInactive,
        Long admins
) {
}
