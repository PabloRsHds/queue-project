package br.com.queue.dtos.schedule.statistics;

import java.util.List;

public record ResponseScheduleDashBoardDto(

        ResponseCountTotalSchedulesStatisticsDto countTotalScheduleStatistics,
        ResponseSchedulePercentagesStatisticsDto schedulePercentagesStatistics,
        List<ResponseSchedulesCreatedByMonthStatisticsDto> schedulesCreatedByMonth,
        List<ResponseSchedulesCreatedByWeekStatisticsDto> schedulesCreatedByWeek,
        ResponseSchedulesCreatedByDayStatisticsDto scheduleCreatedByDay,
        List<ResponseSchedulesByDepartmentStatisticsDto> schedulesByDepartment,
        List<ResponseSchedulesByServiceStatisticsDto> schedulesByService,
        List<ResponseSchedulesByPriorityStatisticsDto> schedulesByPriority,
        List<ResponseSchedulesByHourStatisticsDto> schedulesByHour

) {
}
