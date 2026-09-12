import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { CreateServiceManagementDto } from '../../../dtos/services/CreateServiceManagementDto';
import { ResponseServiceManagementDto } from '../../../dtos/services/ResponseServiceManagementDto';
import { UpdateServiceManagementDto } from '../../../dtos/services/UpdateServiceManagementDto';
import { ResponseGetServiceByIdDto } from '../../../dtos/services/ResponseGetServiceByIdDto';
import { ResponseServiceNamesAndDepartments } from '../../../dtos/services/ResponseServiceNamesAndDepartments';
import { ResponseCountTotalServicesStatisticsDto } from '../../../dtos/services/statistics/ResponseCountTotalServicesStatisticsDto';
import { ResponseSchedulesByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseSchedulesByServiceStatisticsDto';
import { ResponseServicePercentagesStatisticsDto } from '../../../dtos/services/statistics/ResponseServicePercentagesStatisticsDto';
import { ResponseServicesByDepartmentStatisticsDto } from '../../../dtos/services/statistics/ResponseServicesByDepartmentStatisticsDto';
import { ResponseServicesCreatedByMonthStatisticsDto } from '../../../dtos/services/statistics/ResponseServicesCreatedByMonthStatisticsDto';
import { ResponseTicketsByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseTicketsByServiceStatisticsDto';
import { ResponseUsersByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseUsersByServiceStatisticsDto';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ServiceManagementService {

  private http = inject(HttpService);

  // ===== DATA =====
  public services = signal<ResponseServiceManagementDto[]>([]);
  public serviceNamesAndDepartments = signal<ResponseServiceNamesAndDepartments[]>([]);

  public selectedService = signal<ResponseServiceManagementDto | null>(null);

  public registerStatus = signal<'success' | 'error' | 'default'>('default');
  public registerMessage = signal('');

  public updateStatus = signal<'success' | 'error' | 'default'>('default');
  public updateMessage = signal('');

  public deleteStatus = signal<'success' | 'error' | 'default'>('default');
  public deleteMessage = signal('');

  public serviceInfo = signal<ResponseGetServiceByIdDto | null>(null);

  // ===== PAGINATION =====
  public page = signal<number>(0);
  public size = 4;
  public totalElements = signal<number>(0);

  // ===== SEARCH =====
  public search = signal<string>('');

  // ===== MODAL =====
  public modalRegister = signal<Boolean>(false);

  // Statistics
  public countTotalServicesStatistics = signal<ResponseCountTotalServicesStatisticsDto | null>(null);
  public servicePercentagesStatistics = signal<ResponseServicePercentagesStatisticsDto | null>(null);
  public servicesCreatedByMonth = signal<ResponseServicesCreatedByMonthStatisticsDto[] | null>([]);
  public servicesByDepartment = signal<ResponseServicesByDepartmentStatisticsDto[] | null>([]);
  public usersByService = signal<ResponseUsersByServiceStatisticsDto[] | null>([]);
  public schedulesByService = signal<ResponseSchedulesByServiceStatisticsDto[] | null>([]);
  public ticketsByService = signal<ResponseTicketsByServiceStatisticsDto[] | null>([]);

  // ===== Register =======
  registerServiceManagenent(request : CreateServiceManagementDto) {

    this.http.createServiceManagement(request).subscribe({
      next: () => {

        this.page.set(0);
        this.loadServices();

        this.registerMessage.set('Serviço cadastrado com sucesso!');
        this.registerStatus.set('success');

        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.registerMessage.set(error.error?.message || 'Erro ao cadastrar serviço.');
        this.registerStatus.set('error');
      }
    })
  }

  // ===== Update ====
  updateServiceManagement(request : UpdateServiceManagementDto) {

    this.http.updateServiceManagement(request).subscribe({
      next: () => {

        this.page.set(0);
        this.loadServices();

        this.updateMessage.set('Serviço atualizado com sucesso!');
        this.updateStatus.set('success');

        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.updateMessage.set(error.error?.message || 'Erro ao atualizar serviço.');
        this.updateStatus.set('error');
      }
    })
  }

  // ===== Delete =====
  deleteService(serviceManagementId : string) {
    this.http.deleteServiceManagement(serviceManagementId).subscribe({
      next: () => {

        this.page.set(0);
        this.loadServices();

        this.deleteMessage.set('Serviço excluído com sucesso!');
        this.deleteStatus.set('success');

        this.loadStatistics();
      },
      error: (error: HttpErrorResponse) => {
        this.deleteMessage.set(error.error?.message || 'Erro ao excluir serviço.');
        this.deleteStatus.set('error');
      }
    })
  }

  // ===== LOAD SERVICES =====
  loadServices() {
    this.http.getAllServicesManagement(this.page(), this.size, this.search()).subscribe({

      next: (response) => {

        this.services.set(response.content);
        this.loadStatistics();
        this.totalElements.set(response.totalElements);
      },
      error: (error: HttpErrorResponse) => {
        // Tratamento de erro silencioso ou você pode adicionar um signal de erro
        console.error('Erro ao carregar serviços:', error);
      }
    });
  }

  // ===== LOAD SERVICES FOR CREATE USER =====
  loadServiceNamesAndDepartments() {
    this.http.getServiceNamesAndDepartments().subscribe({
      next: (response) => {
        this.serviceNamesAndDepartments.set(response);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar nomes e departamentos:', error);
      }
    })
  }

  // ===== LOAD STATISTICS =====
  loadStatistics() {

    this.http.getServiceStatistics().subscribe({
      next: (response) => {

        this.countTotalServicesStatistics.set(response.countTotalServicesStatistics);
        this.servicePercentagesStatistics.set(response.servicePercentagesStatistics);
        this.servicesCreatedByMonth.set(response.servicesCreatedByMonth);
        this.servicesByDepartment.set(response.servicesByDepartment);
        this.usersByService.set(response.usersByService);
        this.schedulesByService.set(response.schedulesByService);
        this.ticketsByService.set(response.ticketsByService);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas:', error);
      }
    })
  }

  // ==== GET SERVICE BY ID =====
  getInfoService(serviceManagementId : string) {

    this.http.getServiceManagementById(serviceManagementId).subscribe({
      next: (response) => {
        this.serviceInfo.set(response);
      },
      error: (error: HttpErrorResponse) => {
        this.serviceInfo.set(null);
        console.error('Erro ao buscar serviço:', error);
      }
    })
  }

  // ===== PAGINATION =====
  public totalPages = computed(() =>
    Math.ceil(this.totalElements() / this.size)
  );

  nextPage() {
    if (this.page() + 1 >= this.totalPages()) return;

    this.page.update(p => p + 1);
    this.loadServices();
  }

  previousPage() {
    if (this.page() === 0) return;

    this.page.update(p => p - 1);
    this.loadServices();
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages()) return;

    this.page.set(page);
    this.loadServices();
  }

  // ===== SEARCH =====
  setSearch(value: string) {
    this.search.set(value);
    this.page.set(0); // sempre volta pra página 1
    this.loadServices();
  }

  // ===== REFRESH =====
  refresh() {
    this.loadServices();
  }

  resetStatus() {
    this.registerStatus.set('default');
    this.updateStatus.set('default');
    this.deleteStatus.set('default');
  }

  resetInfoService() {
    this.serviceInfo.set(null);
  }
}
