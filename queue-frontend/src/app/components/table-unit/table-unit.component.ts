import { Component, effect, HostListener, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UnitStateService } from '../../services/states/unit/unit-state.service';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-table-unit',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './table-unit.component.html',
  styleUrl: './table-unit.component.css'
})
export class TableUnitComponent {

  // ==================== INJECOES DE DEPENDENCIA ====================

  private unitState = inject(UnitStateService);
  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);

  // ==================== ESTADOS - UNIDADE ====================

  public units = this.unitState.units;
  public unitById = this.unitState.unitById;
  public totalUnits = this.unitState.totalUnits;
  public activeUnits = this.unitState.activeUnits;
  public inactiveUnits = this.unitState.inactiveUnits;

  public createLoading = this.unitState.createLoading;
  public updateLoading = this.unitState.updateLoading;
  public deleteLoading = this.unitState.deleteLoading;
  public allUnitsLoading = this.unitState.allUnitsLoading;

  // ==================== ESTADOS DE PAGINACAO ====================

  public page = this.unitState.page;
  public totalPages = this.unitState.totalPages;
  public totalElements = this.unitState.totalElements;
  public search = this.unitState.search;

  // ==================== VARIAVEIS DE CONTROLE ====================

  private itemsPerPage = 10;

  // ==================== CONTROLE DE MODAIS ====================

  public dropDown: number | null = null;
  public modalRegister: boolean = false;
  public modalUpdate: boolean = false;
  public modalDelete: boolean = false;
  public modalView: boolean = false;

  // ==================== ESTADOS DE RESPONSIVIDADE ====================

  isMobile = signal(window.innerWidth < 768);

  // ==================== FORMULARIO DE REGISTRO ====================

  public registerForm!: FormGroup;

  initializeRegisterForm() {
    this.registerForm = this.fb.group({
      name: [''],
      address: ['']
    });
  }

  // ==================== FORMULARIO DE ATUALIZACAO ====================

  public updateForm!: FormGroup;

  initializeUpdateForm() {
    this.updateForm = this.fb.group({
      name: [''],
      address: [''],
      active: [false]
    });
  }

  // ==================== CICLO DE VIDA ====================

  ngOnInit() {
    this.unitState.getAllUnits();
  }

  // ==================== CONSTRUTOR ====================

  constructor() {

    effect(() => {
      const unit = this.unitById();
      if (this.modalUpdate && unit !== null) {
        this.updateForm.patchValue({
          name: unit?.name,
          address: unit?.address,
          active: unit?.active
        });
      }
    });

    effect(() => {

      if (this.unitState.createStatus() === 'success') {

        this.snackBar.open(this.unitState.createMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.unitState.resetCreateStatus();
        this.closeModalRegister();
      }

      if (this.unitState.createStatus() === 'error') {

        this.snackBar.open(this.unitState.createMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.unitState.resetCreateStatus();
      }
    });

    effect(() => {
      if (this.unitState.updateStatus() === 'success') {

        this.snackBar.open(this.unitState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.unitState.resetUpdateStatus();
        this.closeModalUpdate();
      }
      if (this.unitState.updateStatus() === 'error') {

        this.snackBar.open(this.unitState.updateMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.unitState.resetUpdateStatus();
      }
    });

    effect(() => {
      if (this.unitState.deleteStatus() === 'success') {

        this.snackBar.open(this.unitState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-success']
        });

        this.unitState.resetDeleteStatus();
        this.closeModalDelete();
      }
      if (this.unitState.deleteStatus() === 'error') {

        this.snackBar.open(this.unitState.deleteMessage(), 'Fechar', {
          duration: 3000,
          panelClass: ['snackbar-error']
        });

        this.unitState.resetDeleteStatus();
      }
    });
  }

  // ==================== METODOS DE CONTROLE DE MODAIS ====================

  openModalRegister() {
    this.initializeRegisterForm();
    this.modalRegister = true;
  }

  closeModalRegister() {
    this.modalRegister = false;
  }

  openModalUpdate(unitId: string) {
    this.initializeUpdateForm();
    this.unitState.getUnitById(unitId);
    this.modalUpdate = true;
  }

  closeModalUpdate() {
    this.modalUpdate = false;
    this.unitState.resetUnitById();
  }

  openModalDelete(unitId: string) {
    this.unitState.getUnitById(unitId);
    this.dropDown = null;
    this.modalDelete = true;
  }

  closeModalDelete() {
    this.modalDelete = false;
    this.unitState.resetUnitById();
  }

  openModalView(unitId: string) {
    this.unitState.getUnitById(unitId);
    this.modalView = true;
  }

  closeModalView() {
    this.modalView = false;
    this.unitState.resetUnitById();
  }

  // ==================== METODOS DE CRUD ====================

  registerUnit() {
    if (this.registerForm.invalid) return;
    this.unitState.createUnit(this.registerForm.value);
  }

  updateUnit() {
    if (this.updateForm.invalid) return;
    this.unitState.updateUnit({
      unitId: this.unitById()?.unitId,
      ...this.updateForm.value
    });
  }

  deleteUnit(unitId: string) {
    if (unitId === '') return;
    this.unitState.deleteUnit(unitId);
  }

  // ==================== METODOS DE BUSCA ====================

  onSearch(event: any) {
    this.unitState.setSearch(event.target.value);
  }

  // ==================== METODOS DE PAGINACAO ====================

  nextPage() {
    this.unitState.nextPage();
  }

  previousPage() {
    this.unitState.previousPage();
  }

  goToPage(page: number) {
    this.unitState.goToPage(page);
  }

  getStartIndex(): number {
    return this.page() * this.itemsPerPage + 1;
  }

  getEndIndex(): number {
    return Math.min(
      (this.page() + 1) * this.itemsPerPage,
      this.totalElements()
    );
  }

  getPagesArray(): number[] {
    const total = this.totalPages();
    const current = this.page();

    const maxVisible = 4;

    let start = current - Math.floor(maxVisible / 2);
    let end = current + Math.floor(maxVisible / 2) + 1;

    if (start < 0) {
      start = 0;
      end = Math.min(maxVisible, total);
    }

    if (end > total) {
      end = total;
      start = Math.max(0, total - maxVisible);
    }

    return Array.from(
      { length: end - start },
      (_, i) => start + i
    );
  }

  // ==================== METODOS DE CONTROLE DE DROPDOWN ====================

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInsideDropdown = target.closest('.button-menu-table')
      || target.closest('.drop-down-delete');
    if (!clickedInsideDropdown) {
      this.closeDropDown();
    }
  }

  openDropDown(index: number) {
    if (this.dropDown === index) {
      this.dropDown = null;
      return;
    }
    this.dropDown = index;
  }

  closeDropDown() {
    this.dropDown = null;
  }

  // ==================== METODOS DE RESPONSIVIDADE ====================

  @HostListener('window:resize')
  onResize() {
    this.isMobile.set(window.innerWidth < 768);
  }
}
