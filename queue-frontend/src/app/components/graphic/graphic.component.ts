import { Component, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserStateService } from '../../services/states/user/user-state.service';
import { DepartmentStateService } from '../../services/states/department/department-state.service';
import { ApexAxisChartSeries, ApexChart,ApexXAxis, ApexDataLabels, ApexPlotOptions} from "ng-apexcharts";
import { ServiceManagementService } from '../../services/states/serviceManagement/service-management.service';

import { ChartComponent } from 'ng-apexcharts';
import { CustomerStateService } from '../../services/states/customer/customer-state.service';
import { ScheduleStateService } from '../../services/states/scheduling/scheduling-state.service';
import { AttendentStateService } from '../../services/states/attendent/attendent-state.service';


export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  dataLabels: ApexDataLabels;
};

export type DonutChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  labels: string[];
  colors: string[];
  legend: ApexLegend;
  dataLabels: ApexDataLabels;
  responsive: ApexResponsive[];
};

@Component({
  selector: 'app-graphic',
  imports: [CommonModule, ChartComponent],
  templateUrl: './graphic.component.html',
  styleUrl: './graphic.component.css'
})
export class GraphicComponent {

  // ==================== CONFIGURAÇÕES DOS GRÁFICOS ====================

  /** Configuração do gráfico de departamentos */
  public chartOptions!: ChartOptions;

  /** Configuração do gráfico de serviços */
  public chartServiceOptions!: ChartOptions;

  /** Configuração do gráfico de usuários */
  public chartUserOptions!: ChartOptions;

  /** Configuração do gráfico de clientes */
  public chartCustomerOptions!: ChartOptions;

  /** Configuração do gráfico de agendamentos por mês */
  public chartSchedulingMonthOptions!: ChartOptions;

  /** Configuração do gráfico de agendamentos por semana */
  public chartSchedulingWeekOptions!: ChartOptions;

  /** Configuração do gráfico de agendamentos por hora */
  public chartSchedulingHourOptions!: ChartOptions;

  /** Configuração do gráfico de agendamentos por prioridade (donut) */
  public chartSchedulingPriorityOptions!: DonutChartOptions;

  /** Configuração do gráfico de atendimentos por mês */
  public chartAttendanceMonthOptions!: ChartOptions;

  /** Configuração do gráfico de atendimentos por semana */
  public chartAttendanceWeekOptions!: ChartOptions;

  /** Configuração do gráfico de atendimentos por hora */
  public chartAttendanceHourOptions!: ChartOptions;

  // ==================== INJEÇÕES DE DEPENDÊNCIA ====================

  /** Service para gerenciar estado do usuário */
  public userState = inject(UserStateService);

  /** Service para gerenciar estado do atendente */
  public attendentState = inject(AttendentStateService);

  /** Service para gerenciar estado do departamento */
  public departmentState = inject(DepartmentStateService);

  /** Service para gerenciar estado do serviço */
  public serviceState = inject(ServiceManagementService);

  /** Service para gerenciar estado do cliente */
  public customerState = inject(CustomerStateService);

  /** Service para gerenciar estado do agendamento */
  public schedulingState = inject(ScheduleStateService);

  // ==================== ESTADOS LOCAIS ====================

  /** Usuário atualmente logado */
  public userLogged = this.userState.userLogged;

  /** Valor selecionado para filtro (dia/semana/mês) */
  public selectValue = signal<string>('day');

  // ==================== ESTATÍSTICAS DE ATENDENTE ====================

  /** Total de atendimentos realizados */
  public countTotalAttendances = this.attendentState.countTotalAttendances;

  /** Tempo médio de espera */
  public avarageWaitingTime = this.attendentState.averageWaitingTime;

  /** Tempo médio de serviço */
  public averageServiceTime = this.attendentState.averageServiceTime;

  /** Média de atendimentos por usuário */
  public averageAttendanceByUsers = this.attendentState.averageAttendanceByUser;

  /** Atendimentos criados por mês */
  public attendancesCreatedByMonth = this.attendentState.attendancesCreatedByMonth;

  /** Atendimentos por semana */
  public attendancesByWeek = this.attendentState.attendancesByWeek;

  /** Atendimentos por serviço */
  public attendancesByService = this.attendentState.attendancesByService;

  /** Atendimentos por hora */
  public attendancesByHour = this.attendentState.attendancesByHour;

  /** Atendimentos por departamento */
  public attendancesByDepartment = this.attendentState.attendancesByDepartment;

  /** Atendimentos por cliente */
  public attendancesByCustomer = this.attendentState.attendancesByCustomer;

  // ==================== ESTATÍSTICAS DE DEPARTAMENTO ====================

  /** Total de departamentos */
  public totalDepartments = this.departmentState.countTotalDepartment;

  /** Percentual por departamento */
  public percentageByDepartment = this.departmentState.getPercentagesByDepartment;

  /** Quantidade de serviços por departamento */
  public countServicesByDepartment = this.departmentState.countServicesByDepartment;

  /** Departamentos criados por mês */
  public departmentsCreatedByMonth = this.departmentState.departmentsCreatedByMonth;

  // ==================== ESTATÍSTICAS DE SERVIÇO ====================

  /** Total de serviços */
  public totalServices = this.serviceState.countTotalServicesStatistics;

  /** Percentual de serviços */
  public percentageServices = this.serviceState.servicePercentagesStatistics;

  /** Serviços criados por mês */
  public servicesCreatedByMonth = this.serviceState.servicesCreatedByMonth;

  /** Serviços por departamento */
  public servicesByDepartment = this.serviceState.servicesByDepartment;

  /** Usuários por serviço */
  public usersByService = this.serviceState.usersByService;

  /** Agendamentos por serviço */
  public schedulesByService = this.serviceState.schedulesByService;

  /** Chamados/tickets por serviço */
  public ticketsByService = this.serviceState.ticketsByService;

  // ==================== ESTATÍSTICAS DE USUÁRIO ====================

  /** Total de usuários */
  public totalUsers = this.userState.countTotalUsersStatistics;

  /** Percentual de usuários */
  public percentageUsers = this.userState.userPercentagesStatistics;

  /** Quantidade de serviços por usuário */
  public countServicesByUsers = this.userState.countServicesByUsers;

  /** Quantidade de usuários por cargo */
  public countRoleByUsers = this.userState.countRoleByUsers;

  /** Usuários criados por mês */
  public usersCreatedByMonth = this.userState.usersCreatedByMonthStatistics;

  // ==================== ESTATÍSTICAS DE AGENDAMENTO ====================

  /** Total de agendamentos */
  public totalScheduling = this.schedulingState.countTotalScheduleStatistics;

  /** Percentual de agendamentos */
  public percentageScheduling = this.schedulingState.schedulePercentagesStatistics;

  /** Total de agendamentos por mês */
  public totalSchedulingByMonth = this.schedulingState.schedulesCreatedByMonth;

  /** Total de agendamentos por semana */
  public totalSchedulingByWeek = this.schedulingState.schedulesCreatedByWeek;

  /** Agendamentos criados por dia */
  public scheduleCreatedByDay = this.schedulingState.scheduleCreatedByDay;

  /** Agendamentos por hora */
  public schedulesByHour = this.schedulingState.schedulesByHour;

  /** Agendamentos por departamento */
  public schedulesByDepartment = this.schedulingState.schedulesByDepartment;

  /** Agendamentos por serviço */
  public schedulesByServices = this.schedulingState.schedulesByService;

  /** Agendamentos por prioridade */
  public schedulesByPriority = this.schedulingState.schedulesByPriority;

  // ==================== ESTATÍSTICAS DE CLIENTE ====================

  /** Total de clientes */
  public totalCustomers = this.customerState.totalCustomers;

  /** Total de clientes por mês */
  public totalCustomersByMonth = this.customerState.totalCustomersByMonth;

  /**
   * Inicializa o componente carregando todas as estatísticas
   */
  ngOnInit(): void {
    this.departmentState.loadStatistics();
    this.serviceState.loadStatistics();
    this.userState.loadStatistics();
    this.schedulingState.loadStatistics();
    this.customerState.loadStatistics();
    this.attendentState.loadStatistics();
  }

  constructor() {

    /**
     * Efeito: Configura gráfico de atendimentos por mês
     * Quando dados de atendimentos mensais são carregados, cria gráfico de barras
     */
    effect(() => {

      const data = this.attendancesCreatedByMonth();

      if (!data || data.length === 0) return;

      this.chartAttendanceMonthOptions = {
        series: [{
          name: 'Atendimentos',
          data: data.map(x => x.totalAttendances)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: { show: false }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };

    });

    /**
     * Efeito: Configura gráfico de atendimentos por semana
     * Quando dados de atendimentos semanais são carregados, cria gráfico de barras
     */
    effect(() => {

      const data = this.attendancesByWeek();

      if (!data || data.length === 0) return;

      this.chartAttendanceWeekOptions = {
        series: [{
          name: 'Atendimentos',
          data: data.map(x => x.totalAttendances)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: { show: false }
        },
        xaxis: {
          categories: data.map(x => x.dayName)
        },
        dataLabels: {
          enabled: true
        }
      };

    });

    /**
     * Efeito: Configura gráfico de atendimentos por hora
     * Mapeia atendimentos para horários entre 05h e 22h
     */
    effect(() => {

      const data = this.attendancesByHour();

      if (!data) return;

      // Mapa para localizar os atendimentos por hora
      const attendanceMap = new Map(
        data.map(item => [item.hour, item.totalAttendances])
      );

      // Horários de 05h até 22h
      const hours = Array.from({ length: 18 }, (_, i) => i + 5);

      this.chartAttendanceHourOptions = {
        series: [{
          name: 'Atendimentos',
          data: hours.map(hour => attendanceMap.get(hour) ?? 0)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: { show: false }
        },
        xaxis: {
          categories: hours.map(hour => `${hour.toString().padStart(2, '0')}h`)
        },
        dataLabels: {
          enabled: true
        }
      };

    });

    /**
     * Efeito: Configura gráfico de departamentos criados por mês
     */
    effect(() => {

      const data = this.departmentsCreatedByMonth();

      if (!data || data.length === 0) {
        return;
      }

      this.chartOptions = {
        series: [{
          name: 'Departamentos',
          data: data.map(x => x.totalDepartments)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de serviços criados por mês
     */
    effect(() => {

      const data = this.servicesCreatedByMonth();

      if (!data || data.length === 0) {
        return;
      }

      this.chartServiceOptions = {
        series: [{
          name: 'Serviços',
          data: data.map(x => x.totalServices)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de usuários criados por mês
     */
    effect(() => {

      const data = this.usersCreatedByMonth();

      if (!data || data.length === 0) {
        return;
      }

      this.chartUserOptions = {
        series: [{
          name: 'Usuarios',
          data: data.map(x => x.totalUsers)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de agendamentos por mês
     */
    effect(() => {

      const data = this.totalSchedulingByMonth();

      if (!data || data.length === 0) {
        return;
      }

      this.chartSchedulingMonthOptions = {
        series: [{
          name: 'Agendamentos',
          data: data.map(x => x.totalSchedules)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de agendamentos por semana
     */
    effect(() => {

      const data = this.totalSchedulingByWeek();

      if (!data || data.length === 0) {
        return;
      }

      this.chartSchedulingWeekOptions = {
        series: [{
          name: 'Agendamentos',
          data: data.map(x => x.totalSchedules)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.weekDay)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

    /**
     * Efeito: Configura gráfico de agendamentos por hora
     * Mapeia agendamentos para horários entre 05h e 22h
     */
    effect(() => {

      const data = this.schedulesByHour();

      if (!data) return;

      // Mapa para localizar os agendamentos por hora
      const scheduleMap = new Map(
        data.map(item => [item.hour, item.totalSchedules])
      );

      // Horários de 05h até 22h
      const hours = Array.from({ length: 18 }, (_, i) => i + 5);

      this.chartSchedulingHourOptions = {
        series: [{
          name: 'Agendamentos',
          data: hours.map(hour => scheduleMap.get(hour) ?? 0)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: hours.map(hour => `${hour.toString().padStart(2, '0')}h`)
        },
        dataLabels: {
          enabled: true
        }
      };

    });

    /**
     * Efeito: Configura gráfico de agendamentos por prioridade (donut)
     * Cores: Azul, Verde, Vermelho, Amarelo
     */
    effect(() => {

      const data = this.schedulesByPriority();

      if (!data || data.length === 0) {
        return;
      }

      this.chartSchedulingPriorityOptions = {
        series: data.map(x => x.totalSchedules),

        chart: {
          type: 'donut',
          height: 300
        },

        labels: data.map(x => x.priority),

        colors: [
          '#3b82f6', // Azul
          'tomato', // Verde
          '#ef4444', // Vermelho
          '#f59e0b'  // Amarelo
        ],

        legend: {
          position: 'bottom'
        },

        dataLabels: {
          enabled: true
        },

        responsive: [{
          breakpoint: 768,
          options: {
            chart: {
              width: 300
            },
            legend: {
              position: 'bottom'
            }
          }
        }]
      };
    })

    /**
     * Efeito: Configura gráfico de clientes por mês
     */
    effect(() => {

      const data = this.totalCustomersByMonth();

      if (!data || data.length === 0) {
        return;
      }

      this.chartCustomerOptions = {
        series: [{
          name: 'Clientes',
          data: data.map(x => x.totalCustomers)
        }],
        chart: {
          type: 'bar',
          height: 350,
          toolbar: {
            show: false
          }
        },
        xaxis: {
          categories: data.map(x => x.monthName)
        },
        dataLabels: {
          enabled: true
        }
      };
    });

  };

  // ==================== MÉTODOS PÚBLICOS ====================

  /** Item de navegação ativo (General por padrão) */
  public navItem:string = 'General';

  /**
   * Altera o item de navegação ativo
   * @param item - Nome do item de navegação
   */
  public navItemChange(item: string) {
    this.navItem = item;
  }

  /**
   * Converte o código do cargo para nome amigável em português
   * @param role - Código do cargo (MANAGER, ATTENDANT, RECEPTION)
   * @returns Nome do cargo em português
   */
  public getRoleDisplayName(role: string): string {
    switch (role) {
      case 'MANAGER': return 'Gerente';
      case 'ATTENDANT': return 'Atendente';
      case 'RECEPTION': return 'Recepcionista';
      default: return 'Administrador';
    }
  }

  /**
   * Atualiza o valor selecionado no filtro
   * @param value - Valor do filtro (day, week, month)
   */
  public getSelectValue(value: string) {
    this.selectValue.set(value);
  }

}
