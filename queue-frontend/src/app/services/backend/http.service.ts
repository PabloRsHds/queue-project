import { PageResponse } from '../../dtos/page/PageResponse';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ResponseServiceManagementDto } from '../../dtos/services/ResponseServiceManagementDto';
import { ResponseDepartmentDto } from '../../dtos/department/ResponseDepartmentDto';
import { CreateDepartmentDto } from '../../dtos/department/CreateDepartmentDto';
import { CreateServiceManagementDto } from '../../dtos/services/CreateServiceManagementDto';
import { UpdateDepartmentDto } from '../../dtos/department/UpdateDepartmentDto';
import { UpdateServiceManagementDto } from '../../dtos/services/UpdateServiceManagementDto';
import { ResponseGetServiceByIdDto } from '../../dtos/services/ResponseGetServiceByIdDto';
import { ResponseUserDto } from '../../dtos/users/ResponseUserDto';
import { RequestUserDto } from '../../dtos/users/RequestUserDto';
import { ResponseServiceNamesAndDepartments } from '../../dtos/services/ResponseServiceNamesAndDepartments';
import { ResponseUserInfoDto } from '../../dtos/users/ResponseUserInfoDto';
import { UpdateUserDto } from '../../dtos/users/UpdateUserDto';
import { ResponseAllCustomersDto } from '../../dtos/customer/ResponseAllCustomersDto';
import { ResponseCustomerInfoDto } from '../../dtos/customer/ResponseCustomerInfoDto';
import { CreateCustomerDto } from '../../dtos/customer/CreateCustomerDto';
import { ResponseCustomerDto } from '../../dtos/customer/ResponseCustomerDto';
import { UpdateCustomerDto } from '../../dtos/customer/UpdateCustomerDto';
import { ResponseAllSchedulesDto } from '../../dtos/schedule/ResponseAllSchedulesDto';
import { ResponseCustomerIdsAndNames } from '../../dtos/customer/ResponseCustomerIdsAndNames';
import { CreateScheduleDto } from '../../dtos/schedule/CreateScheduleDto';
import { ResponseScheduleDto } from '../../dtos/schedule/ResponseScheduleDto';
import { UpdateScheduleDto } from '../../dtos/schedule/UpdateScheduleDto';
import { CreateTicketDto } from '../../dtos/ticket/CreateTicketDto';
import { ResponseTicketDto } from '../../dtos/ticket/ResponseTicketDto';
import { ResponseTicketsForAttendanceDto } from '../../dtos/ticket/ResponseTicketsForAttendanceDto';
import { ResponseTokenDto } from '../../dtos/login/ResponseTokenDto';
import { LoginDto } from '../../dtos/login/LoginDto';
import { ResponseDepartmentDashBoardDto } from '../../dtos/department/statistics/ResponseDepartmentDashBoardDto';
import { ResponseServiceDashBoardDto } from '../../dtos/services/statistics/ResponseServiceDashBoardDto';
import { ResponseUserDashBoardDto } from '../../dtos/users/statistics/ResponseUserDashBoardDto';
import { ResponseCustomerDashBoardDto } from '../../dtos/customer/statistics/ResponseCustomerDashBoardDto';
import { ResponseScheduleDashBoardDto } from '../../dtos/schedule/statistics/ResponseScheduleDashBoardDto';
import { ResponseAttendanceDashboardDto } from '../../dtos/attendance/statistics/ResponseAttendanceDashboardDto';
import { StartAttendanceDto } from '../../dtos/attendance/StartAttendanceDto';
import { ResponseAttendanceDto } from '../../dtos/attendance/ResponseAttendanceDto';
import { FinishAttendanceDto } from '../../dtos/attendance/FinishAttendanceDto';
import { ResponseFinishAttendanceDto } from '../../dtos/attendance/ResponseFinishAttendanceDto';
import { ResponseUnitDto } from '../../dtos/unit/ResponseUnitDto';
import { UpdateUnitDto } from '../../dtos/unit/UpdateUnitDto';
import { CreateUnitDto } from '../../dtos/unit/CreateUnitDto';
import { environment } from '../../environments/environment';

declare const process: any;

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  // ============================================================
  // CONFIGURATION
  // ============================================================

  private readonly API_URL = environment.API_URL;

  private http = inject(HttpClient);

  // ============================================================
  // AUTHENTICATION
  // ============================================================

  public login(request: LoginDto): Observable<ResponseTokenDto> {
    return this.http.post<ResponseTokenDto>(
      `${this.API_URL}/login`,
      request,
      { withCredentials: true }
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${this.API_URL}/login/logout`,
      {},
      { withCredentials: true }
    );
  }

  public refreshTokens(): Observable<ResponseTokenDto> {
    return this.http.post<ResponseTokenDto>(
      `${this.API_URL}/login/refresh-tokens`,
      {},
      { withCredentials: true }
    ).pipe(
      catchError((err: HttpErrorResponse) => {

        let errorMsg = 'Serviço de refresh tokens está fora do ar';

        if (err.error?.message) {
          errorMsg = err.error.message;
        }

        return throwError(() => new Error(errorMsg));
      })
    );
  }

  // ============================================================
  // UNITS
  // ============================================================

  public createUnit(request: CreateUnitDto): Observable<ResponseUnitDto> {
    return this.http.post<ResponseUnitDto>(
        `${this.API_URL}/units`,
        request,
        { withCredentials: true }
    );
  }

  public updateUnit(request: UpdateUnitDto): Observable<ResponseUnitDto> {
    return this.http.put<ResponseUnitDto>(
        `${this.API_URL}/units`,
        request,
        { withCredentials: true }
    );
  }

  public deleteUnit(unitId: string): Observable<void> {
    return this.http.delete<void>(
        `${this.API_URL}/units/${unitId}`,
        { withCredentials: true }
    );
  }

  public getAllUnits(page: number, size: number, search?: string): Observable<PageResponse<ResponseUnitDto>> {
    return this.http.get<PageResponse<ResponseUnitDto>>(
        `${this.API_URL}/units?page=${page}&size=${size}&search=${search ?? ''}`,
        { withCredentials: true }
    );
  }

  public getUnitById(unitId: string): Observable<ResponseUnitDto> {

    return this.http.get<ResponseUnitDto>(
        `${this.API_URL}/units/${unitId}`,
        { withCredentials: true }
    );
  }

  // ============================================================
  // DEPARTMENTS
  // ============================================================

  public createDepartment(request: CreateDepartmentDto): Observable<ResponseDepartmentDto> {
    return this.http.post<ResponseDepartmentDto>(
      `${this.API_URL}/departments`,
      request,
      { withCredentials: true }
    );
  }

  public updateDepartment(request: UpdateDepartmentDto): Observable<ResponseDepartmentDto> {
    return this.http.patch<ResponseDepartmentDto>(
      `${this.API_URL}/departments`,
      request,
      { withCredentials: true }
    );
  }

  public deleteDepartment(departmentId: string): Observable<ResponseDepartmentDto> {
    return this.http.delete<ResponseDepartmentDto>(
      `${this.API_URL}/departments/${departmentId}`,
      { withCredentials: true }
    );
  }

  public getAllDepartments(page: number, size: number, search?: string): Observable<PageResponse<ResponseDepartmentDto>> {
    return this.http.get<PageResponse<ResponseDepartmentDto>>(
      `${this.API_URL}/departments?page=${page}&size=${size}&search=${search ?? ''}`,
      { withCredentials: true }
    );
  }

  public getDepartmentById(departmentId: string): Observable<ResponseDepartmentDto> {
    return this.http.get<ResponseDepartmentDto>(
      `${this.API_URL}/departments/${departmentId}`,
      { withCredentials: true }
    );
  }

  public getDepartmentStatistics(): Observable<ResponseDepartmentDashBoardDto> {
    return this.http.get<ResponseDepartmentDashBoardDto>(
      `${this.API_URL}/departments/statistics`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // SERVICES
  // ============================================================

  public createServiceManagement(request: CreateServiceManagementDto): Observable<ResponseServiceManagementDto> {
    return this.http.post<ResponseServiceManagementDto>(
      `${this.API_URL}/services`,
      request,
      { withCredentials: true }
    );
  }

  public updateServiceManagement(request: UpdateServiceManagementDto): Observable<ResponseServiceManagementDto> {
    return this.http.patch<ResponseServiceManagementDto>(
      `${this.API_URL}/services`,
      request,
      { withCredentials: true }
    );
  }

  public deleteServiceManagement(serviceId: string): Observable<ResponseServiceManagementDto> {
    return this.http.delete<ResponseServiceManagementDto>(
      `${this.API_URL}/services/${serviceId}`,
      { withCredentials: true }
    );
  }

  public getAllServicesManagement(page: number, size: number, search?: string): Observable<PageResponse<ResponseServiceManagementDto>> {
    return this.http.get<PageResponse<ResponseServiceManagementDto>>(
      `${this.API_URL}/services?page=${page}&size=${size}&search=${search ?? ''}`,
      { withCredentials: true }
    );
  }

  public getServiceManagementById(serviceId: string): Observable<ResponseGetServiceByIdDto> {
    return this.http.get<ResponseGetServiceByIdDto>(
      `${this.API_URL}/services/${serviceId}`,
      { withCredentials: true }
    );
  }

  public getServiceNamesAndDepartments(): Observable<ResponseServiceNamesAndDepartments[]> {
    return this.http.get<ResponseServiceNamesAndDepartments[]>(
      `${this.API_URL}/services/service-for-created-user`,
      { withCredentials: true }
    );
  }

  public getServiceStatistics(): Observable<ResponseServiceDashBoardDto> {
    return this.http.get<ResponseServiceDashBoardDto>(
      `${this.API_URL}/services/statistics`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // USERS
  // ============================================================

  public createUser(user: RequestUserDto): Observable<ResponseUserDto> {
    return this.http.post<ResponseUserDto>(
      `${this.API_URL}/users`,
      user,
      { withCredentials: true }
    );
  }

  public updateUser(user: UpdateUserDto): Observable<ResponseUserDto> {
    return this.http.patch<ResponseUserDto>(
      `${this.API_URL}/users`,
      user,
      { withCredentials: true }
    );
  }

  public deleteUser(userId: string): Observable<ResponseUserDto> {
    return this.http.delete<ResponseUserDto>(
      `${this.API_URL}/users/${userId}`,
      { withCredentials: true }
    );
  }

  public getAllUsers(page: number, size: number, search?: string): Observable<PageResponse<ResponseUserDto>> {
    return this.http.get<PageResponse<ResponseUserDto>>(
      `${this.API_URL}/users?page=${page}&size=${size}&search=${search ?? ''}`,
      { withCredentials: true }
    );
  }

  public getUserById(userId: string): Observable<ResponseUserInfoDto> {
    return this.http.get<ResponseUserInfoDto>(
      `${this.API_URL}/users/${userId}`,
      { withCredentials: true }
    );
  }

  public getUserByToken(): Observable<ResponseUserInfoDto> {
    return this.http.get<ResponseUserInfoDto>(
      `${this.API_URL}/users/token`,
      { withCredentials: true }
    );
  }

  public getUserStatistics(): Observable<ResponseUserDashBoardDto> {
    return this.http.get<ResponseUserDashBoardDto>(
      `${this.API_URL}/users/statistics`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // CUSTOMERS
  // ============================================================

  public createCustomer(request: CreateCustomerDto): Observable<ResponseCustomerDto> {
    return this.http.post<ResponseCustomerDto>(
      `${this.API_URL}/customers`,
      request,
      { withCredentials: true }
    );
  }

  public updateCustomer(request: UpdateCustomerDto): Observable<ResponseCustomerDto> {
    return this.http.patch<ResponseCustomerDto>(
      `${this.API_URL}/customers`,
      request,
      { withCredentials: true }
    );
  }

  public deleteCustomer(customerId: string): Observable<ResponseCustomerDto> {
    return this.http.delete<ResponseCustomerDto>(
      `${this.API_URL}/customers/${customerId}`,
      { withCredentials: true }
    );
  }

  public getCustomerById(customerId: string): Observable<ResponseCustomerInfoDto> {
    return this.http.get<ResponseCustomerInfoDto>(
      `${this.API_URL}/customers/${customerId}`,
      { withCredentials: true }
    );
  }

  public getCustomerIdsAndNames(): Observable<ResponseCustomerIdsAndNames[]> {
    return this.http.get<ResponseCustomerIdsAndNames[]>(
      `${this.API_URL}/customers/ids-and-names`,
      { withCredentials: true }
    );
  }

  public getAllCustomers(page: number, size: number, search?: string): Observable<PageResponse<ResponseAllCustomersDto>> {
    return this.http.get<PageResponse<ResponseAllCustomersDto>>(
      `${this.API_URL}/customers?page=${page}&size=${size}&search=${search ?? ''}`,
      { withCredentials: true }
    );
  }

  public getCustomerStatistics(): Observable<ResponseCustomerDashBoardDto> {
    return this.http.get<ResponseCustomerDashBoardDto>(
      `${this.API_URL}/customers/statistics`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // SCHEDULES
  // ============================================================

  public createSchedule(request: CreateScheduleDto): Observable<ResponseScheduleDto> {
    return this.http.post<ResponseScheduleDto>(
      `${this.API_URL}/scheduling`,
      request,
      { withCredentials: true }
    );
  }

  public updateSchedule(request: UpdateScheduleDto): Observable<ResponseScheduleDto> {
    return this.http.patch<ResponseScheduleDto>(
      `${this.API_URL}/scheduling`,
      request,
      { withCredentials: true }
    );
  }

  public deleteSchedule(scheduleId: string): Observable<ResponseScheduleDto> {
    return this.http.delete<ResponseScheduleDto>(
      `${this.API_URL}/scheduling/${scheduleId}`,
      { withCredentials: true }
    );
  }

  public getAllScheduling(page: number, size: number, search?: string, scheduleDate?: string | null): Observable<PageResponse<ResponseAllSchedulesDto>> {
    return this.http.get<PageResponse<ResponseAllSchedulesDto>>(
      `${this.API_URL}/scheduling?page=${page}&size=${size}&search=${search ?? ''}&scheduleDate=${scheduleDate ?? ''}`,
      { withCredentials: true }
    );
  }

  public getScheduleById(scheduleId: string): Observable<ResponseScheduleDto> {
    return this.http.get<ResponseScheduleDto>(
      `${this.API_URL}/scheduling/${scheduleId}`,
      { withCredentials: true }
    );
  }

  public getScheduleStatistics(): Observable<ResponseScheduleDashBoardDto> {
    return this.http.get<ResponseScheduleDashBoardDto>(
      `${this.API_URL}/scheduling/statistics`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // TICKETS
  // ============================================================

  public createTicket(request: CreateTicketDto): Observable<ResponseTicketDto> {
    return this.http.post<ResponseTicketDto>(
      `${this.API_URL}/tickets`,
      request,
      { withCredentials: true }
    );
  }

  public deleteTicket(ticketId: string): Observable<ResponseTicketDto> {
    return this.http.delete<ResponseTicketDto>(
      `${this.API_URL}/tickets/${ticketId}`,
      { withCredentials: true }
    );
  }

  public cancelTicket(ticketId: string): Observable<ResponseTicketDto> {
    return this.http.patch<ResponseTicketDto>(
      `${this.API_URL}/tickets/status/${ticketId}`,
      null,
      { withCredentials: true }
    );
  }

  public callTicket(ticketId: string): Observable<ResponseTicketDto> {
    return this.http.patch<ResponseTicketDto>(
      `${this.API_URL}/tickets/call/${ticketId}`,
      null,
      { withCredentials: true }
    );
  }

  public callCustomer(ticketId: string): Observable<ResponseTicketDto> {
    return this.http.patch<ResponseTicketDto>(
      `${this.API_URL}/tickets/call/customer/${ticketId}`,
      null,
      { withCredentials: true }
    );
  }

  public getTicketsForAttendance(page: number, size: number): Observable<PageResponse<ResponseTicketsForAttendanceDto>> {
    return this.http.get<PageResponse<ResponseTicketsForAttendanceDto>>(
      `${this.API_URL}/tickets/tickets-for-attendance?page=${page}&size=${size}`,
      { withCredentials: true }
    );
  }

  public getHistoryTicketsByAttendant(page: number, size: number): Observable<PageResponse<ResponseTicketsForAttendanceDto>> {
    return this.http.get<PageResponse<ResponseTicketsForAttendanceDto>>(
      `${this.API_URL}/tickets/history?page=${page}&size=${size}`,
      { withCredentials: true }
    );
  }

  // ============================================================
  // ATTENDANCES
  // ============================================================

  public startAttendance(request: StartAttendanceDto): Observable<ResponseAttendanceDto> {
    return this.http.post<ResponseAttendanceDto>(
      `${this.API_URL}/attendances`,
      request,
      { withCredentials: true }
    );
  }

  public finishAttendance(request: FinishAttendanceDto): Observable<ResponseFinishAttendanceDto> {
    return this.http.patch<ResponseFinishAttendanceDto>(
      `${this.API_URL}/attendances/finish`,
      request,
      { withCredentials: true }
    );
  }

  public getAttendanceStatistics(): Observable<ResponseAttendanceDashboardDto> {
    return this.http.get<ResponseAttendanceDashboardDto>(
      `${this.API_URL}/attendances/statistics`,
      { withCredentials: true }
    );
  }
}
