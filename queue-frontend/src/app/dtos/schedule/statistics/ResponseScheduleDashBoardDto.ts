import { ResponseCountTotalSchedulesStatisticsDto } from "./ResponseCountTotalSchedulesStatisticsDto";
import { ResponseSchedulePercentagesStatisticsDto } from "./ResponseSchedulePercentagesStatisticsDto";
import { ResponseSchedulesByDepartmentStatisticsDto } from "./ResponseSchedulesByDepartmentStatisticsDto";
import { ResponseSchedulesByHourStatisticsDto } from "./ResponseSchedulesByHourStatisticsDto";
import { ResponseSchedulesByPriorityStatisticsDto } from "./ResponseSchedulesByPriorityStatisticsDto";
import { ResponseSchedulesByServiceStatisticsDto } from "./ResponseSchedulesByServiceStatisticsDto";
import { ResponseSchedulesCreatedByDayStatisticsDto } from "./ResponseSchedulesCreatedByDayStatisticsDto";
import { ResponseSchedulesCreatedByMonthStatisticsDto } from "./ResponseSchedulesCreatedByMonthStatisticsDto";
import { ResponseSchedulesCreatedByWeekStatisticsDto } from "./ResponseSchedulesCreatedByWeekStatisticsDto";

export interface ResponseScheduleDashBoardDto {

  countTotalScheduleStatistics: ResponseCountTotalSchedulesStatisticsDto;
  schedulePercentagesStatistics: ResponseSchedulePercentagesStatisticsDto;
  schedulesCreatedByMonth: ResponseSchedulesCreatedByMonthStatisticsDto[];
  schedulesCreatedByWeek: ResponseSchedulesCreatedByWeekStatisticsDto[];
  scheduleCreatedByDay: ResponseSchedulesCreatedByDayStatisticsDto;
  schedulesByDepartment: ResponseSchedulesByDepartmentStatisticsDto[];
  schedulesByService: ResponseSchedulesByServiceStatisticsDto[];
  schedulesByPriority: ResponseSchedulesByPriorityStatisticsDto[];
  schedulesByHour: ResponseSchedulesByHourStatisticsDto[];
}
