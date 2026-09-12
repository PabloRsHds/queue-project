package br.com.queue.dtos.user.metrics;

import java.math.BigDecimal;

public record ResponseUsersByRoleStatisticsDto(
        String role,
        Long totalUsers,
        BigDecimal percentage
) {
}
