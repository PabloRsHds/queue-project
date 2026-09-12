package br.com.queue.dtos.user.metrics;

public record ResponseUsersCreatedByMonthStatisticsDto(
        Integer month,
        String monthName,
        Long totalUsers
) {
}
