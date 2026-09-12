import { CreateScheduleDto } from './../../../dtos/schedule/CreateScheduleDto';
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { ResponseAllSchedulesDto } from '../../../dtos/schedule/ResponseAllSchedulesDto';
import { ResponseScheduleDto } from '../../../dtos/schedule/ResponseScheduleDto';
import { UpdateScheduleDto } from '../../../dtos/schedule/UpdateScheduleDto';
import { ResponseScheduleStatisticsDto } from '../../../dtos/schedule/ResponseScheduleStatisticsDto';
import { ResponseCountTotalSchedulesStatisticsDto } from '../../../dtos/schedule/statistics/ResponseCountTotalSchedulesStatisticsDto';
import { ResponseSchedulePercentagesStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulePercentagesStatisticsDto';
import { ResponseSchedulesByDepartmentStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByDepartmentStatisticsDto';
import { ResponseSchedulesByHourStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByHourStatisticsDto';
import { ResponseSchedulesByPriorityStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByPriorityStatisticsDto';
import { ResponseSchedulesCreatedByDayStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByDayStatisticsDto';
import { ResponseSchedulesCreatedByMonthStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByMonthStatisticsDto';
import { ResponseSchedulesCreatedByWeekStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByWeekStatisticsDto';
import { ResponseSchedulesByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseSchedulesByServiceStatisticsDto';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ScheduleStateService {

  private http = inject(HttpService);

  // ===== STATES =====

  public schedules = signal<ResponseAllSchedulesDto[]>([]);
  public scheduleInfo = signal<ResponseScheduleDto | null>(null);

  // ===== PAGINATION =====
  public schedulePage = signal(0);
  public scheduleSize = 4;
  public scheduleTotalElements = signal(0);

  public scheduleSearch = signal('');
  public scheduleSearchDate = signal<string | null>(null);

  // ===== MESSAGES =====
  public registerMessage = signal('');
  public registerStatus = signal<'success' | 'error' | 'default'>('default');

  public updateMessage = signal('');
  public updateStatus = signal<'success' | 'error' | 'default'>('default');

  public deleteMessage = signal('');
  public deleteStatus = signal<'success' | 'error' | 'default'>('default');

  // ===== STATISTCS =======
  public countTotalScheduleStatistics = signal<ResponseCountTotalSchedulesStatisticsDto | null>(null);
  public schedulePercentagesStatistics = signal<ResponseSchedulePercentagesStatisticsDto | null>(null);
  public schedulesCreatedByMonth = signal<ResponseSchedulesCreatedByMonthStatisticsDto[] | null>([]);
  public schedulesCreatedByWeek = signal<ResponseSchedulesCreatedByWeekStatisticsDto[] | null>([]);
  public scheduleCreatedByDay = signal<ResponseSchedulesCreatedByDayStatisticsDto | null>(null);
  public schedulesByDepartment = signal<ResponseSchedulesByDepartmentStatisticsDto[] | null>([]);
  public schedulesByService = signal<ResponseSchedulesByServiceStatisticsDto[] | null>([]);
  public schedulesByPriority = signal<ResponseSchedulesByPriorityStatisticsDto[] | null>([]);
  public schedulesByHour = signal<ResponseSchedulesByHourStatisticsDto[] | null>([]);

  // ======== MODAL ========
  public modalSchedulingRegister = signal<Boolean>(false);
  public modalCustomerRegister = signal<Boolean>(false);

  public table = signal<string>('Scheduling');

  public scheduleTotalPages = computed(() =>
    Math.ceil(this.scheduleTotalElements() / this.scheduleSize)
  );

  // ===== METHODS =====

  // Register Schedule
  createSchedule(request: CreateScheduleDto) {
    this.http.createSchedule(request).subscribe({
      next: () => {
        this.registerMessage.set('Agendamento realizado com sucesso!');
        this.registerStatus.set('success');
        this.loadSchedules();
        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.registerMessage.set(error.error?.message || 'Erro ao realizar agendamento');
        this.registerStatus.set('error');
      }
    });
  }

  // Update Schedule
  updateSchedule(request: UpdateScheduleDto) {
    this.http.updateSchedule(request).subscribe({
      next: () => {
        this.updateMessage.set('Agendamento atualizado com sucesso!');
        this.updateStatus.set('success');
        this.loadSchedules();
        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.updateMessage.set(error.error?.message || 'Erro ao atualizar agendamento');
        this.updateStatus.set('error');
      }
    });
  }

  // Delete Schedule
  deleteSchedule(scheduleId: string) {
    this.http.deleteSchedule(scheduleId).subscribe({
      next: () => {
        this.deleteMessage.set('Agendamento deletado com sucesso!');
        this.deleteStatus.set('success');
        this.loadSchedules();
        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.deleteMessage.set(error.error?.message || 'Erro ao deletar agendamento');
        this.deleteStatus.set('error');
      }
    })
  }

  // Statistics
  loadStatistics() {
    this.http.getScheduleStatistics().subscribe({
      next: response => {
        this.countTotalScheduleStatistics.set(response.countTotalScheduleStatistics);
        this.schedulePercentagesStatistics.set(response.schedulePercentagesStatistics);
        this.schedulesCreatedByMonth.set(response.schedulesCreatedByMonth);
        this.schedulesCreatedByWeek.set(response.schedulesCreatedByWeek);
        this.scheduleCreatedByDay.set(response.scheduleCreatedByDay);
        this.schedulesByDepartment.set(response.schedulesByDepartment);
        this.schedulesByService.set(response.schedulesByService);
        this.schedulesByPriority.set(response.schedulesByPriority);
        this.schedulesByHour.set(response.schedulesByHour);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas:', error);
      }
    });
  }

  // Load all schedules
  loadSchedules() {
    this.http.getAllScheduling(
      this.schedulePage(),
      this.scheduleSize,
      this.scheduleSearch(),
      this.scheduleSearchDate()
    ).subscribe({
      next: response => {
        this.schedules.set(response.content);
        this.scheduleTotalElements.set(response.totalElements);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar agendamentos:', error);
      }
    });
  }

  getScheduleById(scheduleId: string) {
    this.http.getScheduleById(scheduleId).subscribe({
      next: response => {
        this.scheduleInfo.set(response);
      },
      error: (error: HttpErrorResponse) => {
        this.scheduleInfo.set(null);
        console.error('Erro ao buscar agendamento:', error);
      }
    });
  }

  nextPage() {
    if (this.schedulePage() + 1 >= this.scheduleTotalPages()) return;

    this.schedulePage.update(p => p + 1);
    this.loadSchedules();
  }

  previousPage() {
    if (this.schedulePage() === 0) return;

    this.schedulePage.update(p => p - 1);
    this.loadSchedules();
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.scheduleTotalPages()) return;

    this.schedulePage.set(page);
    this.loadSchedules();
  }

  setSearch(value: string) {
    this.scheduleSearch.set(value);
    this.schedulePage.set(0);
    this.loadSchedules();
  }

  setSearchDate(value: string | null) {
    this.scheduleSearchDate.set(value);
    this.schedulePage.set(0);
    this.loadSchedules();
  }


  // RESETS
  resetStatus() {
    this.registerStatus.set('default');
    this.updateStatus.set('default')
    this.deleteStatus.set('default');
  }

  resetScheduleInfo() {
    this.scheduleInfo.set(null);
  }
}
