package br.com.queue.dtos.serviceManagement.statistics;

import java.util.List;

public record ResponseServiceDashBoardDto(

        ResponseCountTotalServicesStatisticsDto countTotalServicesStatistics,
        ResponseServicePercentagesStatisticsDto servicePercentagesStatistics,
        List<ResponseServicesCreatedByMonthStatisticsDto> servicesCreatedByMonth,
        List<ResponseServicesByDepartmentStatisticsDto> servicesByDepartment,
        List<ResponseUsersByServiceStatisticsDto> usersByService,
        List<ResponseSchedulesByServiceStatisticsDto> schedulesByService,
        List<ResponseTicketsByServiceStatisticsDto> ticketsByService
) {
}
