package br.com.queue.dtos.department.statistics;

public record ResponseDepartmentsCreatedByMonthStatisticsDto(

        Integer month,
        String monthName,
        Long totalDepartments
) {
}
