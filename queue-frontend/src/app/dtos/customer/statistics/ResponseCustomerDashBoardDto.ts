import { ResponseCountTotalCustomersStatisticsDto } from "./ResponseCountTotalCustomersStatisticsDto";
import { ResponseCustomersCreatedByMonthStatisticsDto } from "./ResponseCustomersCreatedByMonthStatisticsDto";

export interface ResponseCustomerDashBoardDto{
  countTotalCustomersStatistics: ResponseCountTotalCustomersStatisticsDto;
  customersCreatedByMonthStatistics: ResponseCustomersCreatedByMonthStatisticsDto[];
}
