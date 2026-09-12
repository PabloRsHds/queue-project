package br.com.queue.dtos.attendance.statistics;

import java.util.List;

public record ResponseAttendanceDashboardDto(

        ResponseCountTotalAttendancesStatisticsDto countTotalAttendances,

        ResponseAverageWaitingTimeStatisticsDto averageWaitingTime,

        ResponseAverageServiceTimeStatisticsDto averageServiceTime,

        List<ResponseAverageAttendanceByUserStatisticsDto> averageAttendanceByUser,

        List<ResponseAttendancesCreatedByMonthStatisticsDto> attendancesCreatedByMonth,

        List<ResponseAttendancesByWeekStatisticsDto> attendancesByWeek,

        List<ResponseAttendancesByServiceStatisticsDto> attendancesByService,

        List<ResponseAttendancesByHourStatisticsDto> attendancesByHour,

        List<ResponseAttendancesByDepartmentStatisticsDto> attendancesByDepartment,

        List<ResponseAttendancesByCustomerStatisticsDto> attendancesByCustomer

) {
}
