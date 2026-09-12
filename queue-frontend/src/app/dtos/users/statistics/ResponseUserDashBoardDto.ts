import { ResponseCountTotalUsersStatisticsDto } from "./ResponseCountTotalUsersStatisticsDto";
import { ResponseServicesByUserStatisticsDto } from "./ResponseServicesByUserStatisticsDto";
import { ResponseUserPercentagesStatisticsDto } from "./ResponseUserPercentagesStatisticsDto";
import { ResponseUsersByRoleStatisticsDto } from "./ResponseUsersByRoleStatisticsDto";
import { ResponseUsersCreatedByMonthStatisticsDto } from "./ResponseUsersCreatedByMonthStatisticsDto";

export interface ResponseUserDashBoardDto{

  countTotalUsersStatistics: ResponseCountTotalUsersStatisticsDto;
  userPercentagesStatistics: ResponseUserPercentagesStatisticsDto;
  usersCreatedByMonthStatistics: ResponseUsersCreatedByMonthStatisticsDto[];
  countServicesByUsers: ResponseServicesByUserStatisticsDto[];
  countRoleByUsers: ResponseUsersByRoleStatisticsDto[];
}
