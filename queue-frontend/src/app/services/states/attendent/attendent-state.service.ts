import { inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { ResponseCountTotalAttendancesStatisticsDto } from '../../../dtos/attendance/statistics/ResponseCountTotalAttendancesStatisticsDto';
import { ResponseAverageServiceTimeStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageServiceTimeStatisticsDto';
import { ResponseAttendancesByCustomerStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByCustomerStatisticsDto';
import { ResponseAttendancesByDepartmentStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByDepartmentStatisticsDto';
import { ResponseAttendancesByHourStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByHourStatisticsDto';
import { ResponseAttendancesByServiceStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByServiceStatisticsDto';
import { ResponseAttendancesByWeekStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByWeekStatisticsDto';
import { ResponseAttendancesCreatedByMonthStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesCreatedByMonthStatisticsDto';
import { ResponseAverageAttendanceByUserStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageAttendanceByUserStatisticsDto';
import { ResponseAverageWaitingTimeStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageWaitingTimeStatisticsDto';
import { TicketStateService } from '../ticket/ticket-state.service';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AttendentStateService {

  // Injections
  private http = inject(HttpService);
  private ticketState = inject(TicketStateService);

  // Attendance States
  public countTotalAttendances = signal<ResponseCountTotalAttendancesStatisticsDto | null>(null);
  public averageWaitingTime = signal<ResponseAverageWaitingTimeStatisticsDto | null>(null);
  public averageServiceTime = signal<ResponseAverageServiceTimeStatisticsDto | null>(null)
  public averageAttendanceByUser = signal<ResponseAverageAttendanceByUserStatisticsDto[] | null>(null);
  public attendancesCreatedByMonth = signal<ResponseAttendancesCreatedByMonthStatisticsDto[] | null>(null);
  public attendancesByWeek = signal<ResponseAttendancesByWeekStatisticsDto[] | null>(null);
  public attendancesByService = signal<ResponseAttendancesByServiceStatisticsDto[] | null>(null)
  public attendancesByHour = signal<ResponseAttendancesByHourStatisticsDto[] | null>(null);
  public attendancesByDepartment = signal<ResponseAttendancesByDepartmentStatisticsDto[] | null>(null)
  public attendancesByCustomer = signal<ResponseAttendancesByCustomerStatisticsDto[] | null>(null);

  // Time
  public currentTimer = signal<string>('00:00:00');
  private intervalId: any;


  // Messages
  public startAttendanceMessage = signal('');
  public startAttendanceStatus = signal<'success' | 'error' | 'default'>('default');

  public finishAttendanceMessage = signal('');
  public finishAttendanceStatus = signal<'success' | 'error' | 'default'>('default');

  public cancelAttendanceMessage = signal('');
  public cancelAttendanceStatus = signal<'success' | 'error' | 'default'>('default');

  loadStatistics() {
    return this.http.getAttendanceStatistics().subscribe({
      next: (response) => {
        this.countTotalAttendances.set(response.countTotalAttendances);
        this.averageWaitingTime.set(response.averageWaitingTime);
        this.averageServiceTime.set(response.averageServiceTime);
        this.averageAttendanceByUser.set(response.averageAttendanceByUser);
        this.attendancesCreatedByMonth.set(response.attendancesCreatedByMonth);
        this.attendancesByWeek.set(response.attendancesByWeek);
        this.attendancesByService.set(response.attendancesByService);
        this.attendancesByHour.set(response.attendancesByHour);
        this.attendancesByDepartment.set(response.attendancesByDepartment);
        this.attendancesByCustomer.set(response.attendancesByCustomer);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas de atendimento:', error);
      }
    });
  }

  startAttendance(ticketId: string) {
    return this.http.startAttendance({ ticketId }).subscribe({
      next: (response) => {

        this.startTimer(response.startedAt);
        this.loadStatistics();
        this.ticketState.getTicketsForAttendence();

        this.startAttendanceMessage.set("Atendimento iniciado com sucesso!");
        this.startAttendanceStatus.set('success');
      },
      error: (error: HttpErrorResponse) => {
        this.startAttendanceMessage.set(error.error?.message || "Erro ao iniciar atendimento!");
        this.startAttendanceStatus.set('error');
      }
    });
  }

  finishAttendance(ticketId: string, resolution: string) {
    return this.http.finishAttendance({ ticketId, resolution }).subscribe({
      next: () => {
        this.stopTimer();
        this.loadStatistics();
        this.ticketState.getTicketsForAttendence();

        this.finishAttendanceMessage.set("Atendimento finalizado com sucesso!");
        this.finishAttendanceStatus.set('success');
      },
      error: (error: HttpErrorResponse) => {
        this.finishAttendanceMessage.set(error.error?.message || "Erro ao finalizar atendimento!");
        this.finishAttendanceStatus.set('error');
      }
    });
  }

  cancelAttendance(ticketId: string) {

    this.http.cancelTicket(ticketId).subscribe({
      next: () => {
        this.ticketState.getTicketsForAttendence();

        this.cancelAttendanceMessage.set("Atendimento cancelado com sucesso!");
        this.cancelAttendanceStatus.set('success');
      },
      error: (error: HttpErrorResponse) => {
        this.cancelAttendanceMessage.set(error.error?.message || "Erro ao cancelar atendimento!");
        this.cancelAttendanceStatus.set('error');
      }
    })
  }

  // Timer
  startTimer(startedAt: string): void {

    const startDate = new Date(startedAt);

    if (this.intervalId) {
      clearInterval(this.intervalId);
    }

    this.intervalId = setInterval(() => {

      const diff = Math.floor((Date.now() - startDate.getTime()) / 1000);

      const hours = Math.floor(diff / 3600);
      const minutes = Math.floor((diff % 3600) / 60);
      const seconds = diff % 60;

      this.currentTimer.set(
        `${hours.toString().padStart(2, '0')}:` +
        `${minutes.toString().padStart(2, '0')}:` +
        `${seconds.toString().padStart(2, '0')}`
      );

    }, 1000);
  }

  stopTimer(): void {

    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = undefined;
    }

    this.currentTimer.set('00:00:00');
  }

  resetStatus() {
    this.startAttendanceMessage.set('');
    this.startAttendanceStatus.set('default');
    this.finishAttendanceMessage.set('');
    this.finishAttendanceStatus.set('default');
    this.cancelAttendanceMessage.set('');
    this.cancelAttendanceStatus.set('default');
  }

  // Increment
  incrementWaitingAttendances() {
    this.countTotalAttendances.update(state => {

      if (!state) return state;

      return {
        ...state,
        countAttendancesWaiting: state.countAttendancesWaiting + 1
      };

    });
  }
}
