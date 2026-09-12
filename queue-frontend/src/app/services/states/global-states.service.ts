import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GlobalStatesService {

  public activeSectionValue = localStorage.getItem('activeSection');

  public activeSection = signal<string>(this.activeSectionValue || 'inicio');
  public activeFunction = signal<string>('');
  public openModalTableService = signal<boolean>(false);

  public openCardDepartment = signal<boolean>(false);
  public currentSection = signal<'departamento' | 'servico' | 'config'>('departamento');

  public openRegisterDepartment = signal<boolean>(false);
  public openTableDeparments = signal<boolean>(false);

  public openRegisterService = signal<boolean>(false);
  public openTableServices = signal<boolean>(false);


  closeModalRegisterService() {
    this.openRegisterService.set(false);
  }

  closeModalRegisterDepartment() {
    this.openRegisterDepartment.set(false);
  }

  closeModalTableDeparments() {
    this.openTableDeparments.set(false);
  }

  closeModalTableServices() {
    this.openTableServices.set(false);
  }
}
