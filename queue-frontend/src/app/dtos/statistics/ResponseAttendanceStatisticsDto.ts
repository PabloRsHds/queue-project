export interface ResponseAttendanceStatisticsDto {

  countAttendancesWaiting: number,
  countAttendancesInProgress: number,
  countAttendancesOfDay: number,
  averageWaitingTime: string,
  averageServiceTime: string
}
