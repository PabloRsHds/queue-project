package br.com.queue.dtos.serviceManagement.statistics;

import java.math.BigDecimal;

public record ResponseTicketsByServiceStatisticsDto(
        String serviceName,
        Long totalTickets,
        BigDecimal percentage
) {
}
