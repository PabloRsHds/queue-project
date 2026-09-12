import { ResponseSchedulesByServiceStatisticsDto } from './ResponseSchedulesByServiceStatisticsDto';
import { ResponseCountTotalServicesStatisticsDto } from "./ResponseCountTotalServicesStatisticsDto"
import { ResponseServicePercentagesStatisticsDto } from "./ResponseServicePercentagesStatisticsDto";
import { ResponseServicesByDepartmentStatisticsDto } from "./ResponseServicesByDepartmentStatisticsDto";
import { ResponseServicesCreatedByMonthStatisticsDto } from "./ResponseServicesCreatedByMonthStatisticsDto";
import { ResponseTicketsByServiceStatisticsDto } from "./ResponseTicketsByServiceStatisticsDto";
import { ResponseUsersByServiceStatisticsDto } from "./ResponseUsersByServiceStatisticsDto";


export interface ResponseServiceDashBoardDto {

  countTotalServicesStatistics: ResponseCountTotalServicesStatisticsDto;
  servicePercentagesStatistics: ResponseServicePercentagesStatisticsDto;
  servicesCreatedByMonth: ResponseServicesCreatedByMonthStatisticsDto[];
  servicesByDepartment: ResponseServicesByDepartmentStatisticsDto[];
  usersByService: ResponseUsersByServiceStatisticsDto[];
  schedulesByService: ResponseSchedulesByServiceStatisticsDto[];
  ticketsByService: ResponseTicketsByServiceStatisticsDto[]
}
