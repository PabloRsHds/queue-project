package br.com.queue.dtos.customer.statistics;

public record ResponseCustomersCreatedByMonthStatisticsDto(
        Integer month,
        String monthName,
        Long totalCustomers
) {
}
