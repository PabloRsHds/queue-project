package br.com.queue.dtos.user.metrics;

public record ResponseServicesByUserStatisticsDto(
        String username,
        Long totalServices
) {
}
