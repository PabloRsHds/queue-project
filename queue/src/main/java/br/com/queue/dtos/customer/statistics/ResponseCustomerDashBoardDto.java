package br.com.queue.dtos.customer.statistics;

import java.util.List;

public record ResponseCustomerDashBoardDto(
        ResponseCountTotalCustomersStatisticsDto countTotalCustomersStatistics,
        List<ResponseCustomersCreatedByMonthStatisticsDto> customersCreatedByMonthStatistics
) {
}
