import { ResponseAttendancesByCustomerStatisticsDto } from "./ResponseAttendancesByCustomerStatisticsDto";
import { ResponseAttendancesByDepartmentStatisticsDto } from "./ResponseAttendancesByDepartmentStatisticsDto";
import { ResponseAttendancesByHourStatisticsDto } from "./ResponseAttendancesByHourStatisticsDto";
import { ResponseAttendancesByServiceStatisticsDto } from "./ResponseAttendancesByServiceStatisticsDto";
import { ResponseAttendancesByWeekStatisticsDto } from "./ResponseAttendancesByWeekStatisticsDto";
import { ResponseAttendancesCreatedByMonthStatisticsDto } from "./ResponseAttendancesCreatedByMonthStatisticsDto";
import { ResponseAverageAttendanceByUserStatisticsDto } from "./ResponseAverageAttendanceByUserStatisticsDto";
import { ResponseAverageServiceTimeStatisticsDto } from "./ResponseAverageServiceTimeStatisticsDto";
import { ResponseAverageWaitingTimeStatisticsDto } from "./ResponseAverageWaitingTimeStatisticsDto";
import { ResponseCountTotalAttendancesStatisticsDto } from "./ResponseCountTotalAttendancesStatisticsDto";

export interface ResponseAttendanceDashboardDto {

    countTotalAttendances: ResponseCountTotalAttendancesStatisticsDto;
    averageWaitingTime: ResponseAverageWaitingTimeStatisticsDto;
    averageServiceTime: ResponseAverageServiceTimeStatisticsDto;
    averageAttendanceByUser: ResponseAverageAttendanceByUserStatisticsDto[];
    attendancesCreatedByMonth: ResponseAttendancesCreatedByMonthStatisticsDto[];
    attendancesByWeek: ResponseAttendancesByWeekStatisticsDto[];
    attendancesByService: ResponseAttendancesByServiceStatisticsDto[];
    attendancesByHour: ResponseAttendancesByHourStatisticsDto[];
    attendancesByDepartment: ResponseAttendancesByDepartmentStatisticsDto[];
    attendancesByCustomer: ResponseAttendancesByCustomerStatisticsDto[];
}