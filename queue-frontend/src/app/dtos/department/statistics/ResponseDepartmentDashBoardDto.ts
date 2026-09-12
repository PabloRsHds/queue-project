import { ResponseCountServicesByDepartmentsStatisticsDto } from "./ResponseCountServicesByDepartmentsStatisticsDto"
import { ResponseCountTotalDepartmentsStatisticsDto } from "./ResponseCountTotalDepartmentsStatisticsDto"
import { ResponseDepartmentPercentagesStatisticsDto } from "./ResponseDepartmentPercentagesStatisticsDto"
import { ResponseDepartmentsCreatedByMonthStatisticsDto } from "./ResponseDepartmentsCreatedByMonthStatisticsDto";

export interface ResponseDepartmentDashBoardDto {

  countTotalDepartmentsStatistics: ResponseCountTotalDepartmentsStatisticsDto;
  countServicesByDepartments: ResponseCountServicesByDepartmentsStatisticsDto[];
  departmentPercentagesStatistics: ResponseDepartmentPercentagesStatisticsDto;
  departmentsCreatedByMonthStatistics: ResponseDepartmentsCreatedByMonthStatisticsDto[]
}
