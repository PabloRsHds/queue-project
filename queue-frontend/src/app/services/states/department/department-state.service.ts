import { computed, inject, Injectable, signal } from '@angular/core';

import { HttpService } from '../../backend/http.service';

import { ResponseDepartmentDto } from '../../../dtos/department/ResponseDepartmentDto';
import { CreateDepartmentDto } from '../../../dtos/department/CreateDepartmentDto';
import { UpdateDepartmentDto } from '../../../dtos/department/UpdateDepartmentDto';
import { ResponseCountTotalDepartmentsStatisticsDto } from '../../../dtos/department/statistics/ResponseCountTotalDepartmentsStatisticsDto';
import { ResponseCountServicesByDepartmentsStatisticsDto } from '../../../dtos/department/statistics/ResponseCountServicesByDepartmentsStatisticsDto';
import { ResponseDepartmentPercentagesStatisticsDto } from '../../../dtos/department/statistics/ResponseDepartmentPercentagesStatisticsDto';
import { ResponseDepartmentsCreatedByMonthStatisticsDto } from '../../../dtos/department/statistics/ResponseDepartmentsCreatedByMonthStatisticsDto';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class DepartmentStateService {

  // ===================== INJECTIONS =====================

  private http = inject(HttpService);

  // ===================== DATA =====================

  public departments = signal<ResponseDepartmentDto[]>([]);
  public departmentInfo = signal<ResponseDepartmentDto | null>(null);
  public departmentNames = signal<string[] | null>([]);

  // Statistics
  public countServicesByDepartment  = signal<ResponseCountServicesByDepartmentsStatisticsDto[] | null>([]);
  public countTotalDepartment = signal<ResponseCountTotalDepartmentsStatisticsDto | null>(null);
  public getPercentagesByDepartment = signal<ResponseDepartmentPercentagesStatisticsDto | null>(null);
  public departmentsCreatedByMonth = signal<ResponseDepartmentsCreatedByMonthStatisticsDto[] | null>([]);


  // Modal
  public modalRegister = signal<Boolean>(false);

  // ===================== LOADING =====================

  public loading = signal(false);

  // ===================== RESPONSE STATUS =====================

  public registerMessage = signal('');
  public registerStatus = signal<'success' | 'error' | 'default'>('default');

  public updateMessage = signal('');
  public updateStatus = signal<'success' | 'error' | 'default'>('default');

  public deleteMessage = signal('');
  public deleteStatus = signal<'success' | 'error' | 'default'>('default');

  // ====================== LOADING ===========================
  
  public allDepartmentsLoading = signal(false);
  public registerLoading = signal(false);
  public updateLoading = signal(false);
  public deleteLoading = signal(false);

  // ===================== PAGINATION =====================

  public page = signal(0);
  public readonly size = 4;
  public totalElements = signal(0);

  public totalPages = computed(() =>
    Math.ceil(this.totalElements() / this.size)
  );

  // ===================== SEARCH =====================

  public search = signal('');

  // ===================== LOAD =====================

  loadDepartments() {

    this.allDepartmentsLoading.set(true);

    this.http.getAllDepartments(this.page(), this.size, this.search()).subscribe({

      next: (response) => {
        this.departments.set(response.content);
        this.loadStatistics();
        this.totalElements.set(response.totalElements);

        this.allDepartmentsLoading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar departamentos', error.error?.message || error.message || 'Erro desconhecido');
        this.allDepartmentsLoading.set(false);
      }
    });
  }

  // ========== LOAD STATISTICS ==========
  loadStatistics() {

    this.http.getDepartmentStatistics().subscribe({
      next: (response) => {
        this.countServicesByDepartment.set(response.countServicesByDepartments);
        this.countTotalDepartment.set(response.countTotalDepartmentsStatistics);
        this.getPercentagesByDepartment.set(response.departmentPercentagesStatistics);
        this.departmentsCreatedByMonth.set(response.departmentsCreatedByMonthStatistics);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas', error.error?.message || error.message || 'Erro desconhecido');
      }
    })
  }

  // ===================== CREATE =====================

  createDepartment(create: CreateDepartmentDto) {
    this.registerLoading.set(true);

    this.http.createDepartment(create).subscribe({
      next: (response) => {

        // volta pra primeira página
        this.page.set(0);

        // backend recalcula paginação
        this.departments.update(
          departments => {
            this.totalElements.update(total => total + 1);
            return [response, ...departments];
          } 
        );
        
        this.loadStatistics();

        this.registerMessage.set('Departamento criado com sucesso!');
        this.registerStatus.set('success');
        this.registerLoading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        this.registerMessage.set(error.error?.message || 'Erro ao criar departamento');
        this.registerStatus.set('error');
        this.registerLoading.set(false);
      }
    });
  }

  // ===================== UPDATE =====================

  updateDepartment(department: UpdateDepartmentDto) {
    this.updateLoading.set(true);

    this.http.updateDepartment(department).subscribe({

      next: (response) => {

        // sincroniza com backend
        this.departments.update( departments => 
          departments.map(
            department => 
              department.departmentId === response.departmentId
              ? response : department     
          )
        );
        
        this.loadStatistics();
        this.getInfoDepartment(response.departmentId);

        this.updateMessage.set('Departamento atualizado com sucesso!');
        this.updateStatus.set('success');
        this.updateLoading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        this.updateMessage.set(error.error?.message || 'Erro ao atualizar departamento');
        this.updateStatus.set('error');
        this.updateLoading.set(false);
      }
    });
  }

  // ===================== DELETE =====================

  deleteDepartment(departmentId: string) {
    this.deleteLoading.set(true);

    this.http.deleteDepartment(departmentId).subscribe({

      next: () => {

        // se deletou o último item da página
        // volta uma página
        if (this.departments().length === 1 && this.page() > 0
        ) {
          this.page.update(p => p - 1);
        }

        // backend recalcula tudo
        this.departments.update(departments =>
          departments.filter(department => {
            this.totalElements.update(total => total - 1);
            return department.departmentId !== departmentId;
          })
        );
        this.loadStatistics();

        // limpa detalhe
        this.departmentInfo.set(null);

        this.deleteMessage.set('Departamento excluído com sucesso!');
        this.deleteStatus.set('success');
        this.deleteLoading.set(false);
      },

      error: (error: HttpErrorResponse) => {
        this.deleteMessage.set(error.error?.message || 'Erro ao excluir departamento');
        this.deleteStatus.set('error');
        this.deleteLoading.set(false);
      }
    });
  }

  // ===================== DETAILS =====================

  getInfoDepartment(departmentId: string) {

    const department = this.departments().find(department => department.departmentId === departmentId);

    if (department) {
      this.departmentInfo.set(department);
      return;
    }
  }

  // ================= GET DEPARTMENT NAMES =================

  loadNamesOfDepartments() {
    const names = this.departments().map(department => department.name);

    if (names) {
      this.departmentNames.set(names)
      return;
    }
  }

  // ===================== PAGINATION =====================

  nextPage() {

    if (this.page() + 1 >= this.totalPages()) return;

    this.page.update(p => p + 1);

    this.loadDepartments();
  }

  previousPage() {

    if (this.page() === 0) return;
    this.page.update(p => p - 1);

    this.loadDepartments();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.totalPages()) return;
    this.page.set(page);

    this.loadDepartments();
  }

  // ===================== SEARCH =====================

  setSearch(value: string) {

    this.search.set(value);
    // volta pra primeira página
    this.page.set(0);

    this.loadDepartments();
  }

  // ===================== RESET =====================

  resetLoadDepartments() {
    this.loadDepartments();
  }

  resetStatus() {
    this.registerStatus.set('default');
    this.updateStatus.set('default');
    this.deleteStatus.set('default');
  }

  resetDepartmentInfo() {
    this.departmentInfo.set(null);
  }

  resetDepartmentNames() {
    this.departmentNames.set([]);
  }
}
