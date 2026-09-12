import { computed, inject, Injectable, signal } from '@angular/core';
import { ResponseUnitDto } from '../../../dtos/unit/ResponseUnitDto';
import { CreateUnitDto } from '../../../dtos/unit/CreateUnitDto';
import { UpdateUnitDto } from '../../../dtos/unit/UpdateUnitDto';
import { HttpService } from '../../backend/http.service';
import { PageResponse } from '../../../dtos/page/PageResponse';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UnitStateService {

  // INJECTIONS
  private http = inject(HttpService);

  public createStatus = signal<'success' | 'error' | 'default'>('default');
  public createMessage = signal('');

  public updateStatus = signal<'success' | 'error' | 'default'>('default');
  public updateMessage = signal('');

  public deleteStatus = signal<'success' | 'error' | 'default'>('default');
  public deleteMessage = signal('');

  public loadStatus = signal<'loading' | 'success' | 'error' | 'default'>('default');
  public loadMessage = signal('');


  // Loading
  public allUnitsLoading = signal(false);
  public createLoading = signal(false);
  public updateLoading = signal(false);
  public deleteLoading = signal(false);

  // PAGINATION
  public page = signal<number>(0);
  public size = signal<number>(4);
  public search = signal<string>('');
  public totalElements = signal<number>(0);
  public totalPages = signal<number>(0);

  // STATES
  public units = signal<ResponseUnitDto[]>([]);

  // COMPUTED
  public totalUnits = computed(() => this.units().length);
  public activeUnits = computed(() => this.units().filter(u => u.active).length);
  public inactiveUnits = computed(() => this.units().filter(u => !u.active).length);

  public unitById = signal<ResponseUnitDto | null>(null);

  // ============================================================
  // GET ALL UNITS
  // ============================================================

  getAllUnits() {

    this.allUnitsLoading.set(true);

    this.http.getAllUnits(this.page(), this.size(), this.search()).subscribe({
      next: (response) => {
        this.units.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.allUnitsLoading.set(false);
        this.loadStatus.set('success');
        this.loadMessage.set('Unidades carregadas com sucesso!');
      },
      error: (error: HttpErrorResponse) => {
        this.loadStatus.set('error');
        this.loadMessage.set(error.error?.message || 'Erro ao carregar unidades');
        this.allUnitsLoading.set(false);
      }
    });
  }

  // ============================================================
  // GET ALL UNITS FOR LOGIN (com size grande)
  // ============================================================

  getAllUnitsForLogin() {
    this.loadStatus.set('loading');
    this.loadMessage.set('Carregando unidades...');

    this.http.getAllUnits(0, 999).subscribe({
      next: (response: PageResponse<ResponseUnitDto>) => {
        this.units.set(response.content);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.loadStatus.set('success');
        this.loadMessage.set('Unidades carregadas com sucesso!');
      },
      error: (error: HttpErrorResponse) => {
        this.loadStatus.set('error');
        this.loadMessage.set(error.error?.message || 'Erro ao carregar unidades');
        console.error('Erro ao carregar unidades:', error);
      }
    });
  }

  // ============================================================
  // GET UNIT BY ID
  // ============================================================

  getUnitById(unitId: string) {
    const unit = this.units().find(unit => unit.unitId === unitId);

    if (unit) {
      this.unitById.set(unit);
      return;
    }

    this.http.getUnitById(unitId).subscribe({
      next: (response) => {
        this.unitById.set(response);
      },
      error: (error: HttpErrorResponse) => {
        console.error('Erro ao carregar unidade:', error);
      }
    });
  }

  resetUnitById() {
    this.unitById.set(null);
  }

  // ============================================================
  // CREATE UNIT
  // ============================================================

  createUnit(request: CreateUnitDto) {

    this.createLoading.set(true);

    this.http.createUnit(request).subscribe({
      next: (response) => {
        this.createStatus.set('success');
        this.createMessage.set('Unidade criada com sucesso!');
        this.units.update(units => {
          this.totalElements.update(total => total + 1);
          return [response, ...units];
        });
        this.createLoading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.createStatus.set('error');
        this.createMessage.set(error.error?.message || 'Erro ao criar unidade');
        this.createLoading.set(false);
      }
    });
  }

  // ============================================================
  // UPDATE UNIT
  // ============================================================

  updateUnit(request: UpdateUnitDto) {

    this.updateLoading.set(true);

    this.http.updateUnit(request).subscribe({
      next: (response) => {
        this.updateStatus.set('success');
        this.updateMessage.set('Unidade atualizada com sucesso!');
        this.units.update(
          units => units.map(unit => unit.unitId === response.unitId ? response : unit)
        )
        this.updateLoading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.updateStatus.set('error');
        this.updateMessage.set(error.error?.message || 'Erro ao atualizar unidade');
        this.updateLoading.set(false);
      }
    });
  }

  // ============================================================
  // DELETE UNIT
  // ============================================================

  deleteUnit(unitId: string) {

    this.deleteLoading.set(true);

    this.http.deleteUnit(unitId).subscribe({
      next: () => {
        this.deleteStatus.set('success');
        this.deleteMessage.set('Unidade excluída com sucesso!');
        this.units.update(units => {
          this.totalElements.update(total => total - 1);
          return units.filter(unit => unit.unitId !== unitId);
        });
        this.unitById.set(null);
        this.deleteLoading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.deleteStatus.set('error');
        this.deleteMessage.set(error.error?.message || 'Erro ao excluir unidade');
        console.error('Erro ao excluir unidade:', error);
        this.deleteLoading.set(false);
      }
    });
  }

  // ============================================================
  // PAGINATION
  // ============================================================

  setPage(page: number) {
    this.page.set(page);
    this.getAllUnits();
  }

  setSize(size: number) {
    this.size.set(size);
    this.page.set(0);
    this.getAllUnits();
  }

  setSearch(search: string) {
    this.search.set(search);
    this.page.set(0);
    this.getAllUnits();
  }

  nextPage() {
    if (this.page() + 1 < this.totalPages()) {
      this.page.update(p => p + 1);
      this.getAllUnits();
    }
  }

  previousPage() {
    if (this.page() > 0) {
      this.page.update(p => p - 1);
      this.getAllUnits();
    }
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.page.set(page);
      this.getAllUnits();
    }
  }

  // ============================================================
  // RESETS
  // ============================================================

  resetCreateStatus() {
    this.createStatus.set('default');
    this.createMessage.set('');
  }

  resetUpdateStatus() {
    this.updateStatus.set('default');
    this.updateMessage.set('');
  }

  resetDeleteStatus() {
    this.deleteStatus.set('default');
    this.deleteMessage.set('');
  }

  resetLoadStatus() {
    this.loadStatus.set('default');
    this.loadMessage.set('');
  }

  resetAllStatus() {
    this.resetCreateStatus();
    this.resetUpdateStatus();
    this.resetDeleteStatus();
    this.resetLoadStatus();
  }

  // ============================================================
  // REFRESH UNITS
  // ============================================================

  refreshUnits() {
    this.getAllUnits();
  }
}
