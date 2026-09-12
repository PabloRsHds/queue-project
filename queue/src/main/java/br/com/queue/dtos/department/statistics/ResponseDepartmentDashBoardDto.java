package br.com.queue.dtos.department.statistics;

import java.util.List;

public record ResponseDepartmentDashBoardDto(
        ResponseCountTotalDepartmentsStatisticsDto countTotalDepartmentsStatistics,
        List<ResponseCountServicesByDepartmentsStatisticsDto> countServicesByDepartments,
        ResponseDepartmentPercentagesStatisticsDto departmentPercentagesStatistics,
        List<ResponseDepartmentsCreatedByMonthStatisticsDto> departmentsCreatedByMonthStatistics
) {
}
