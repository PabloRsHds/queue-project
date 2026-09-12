import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableDepartmentsComponent } from './table-departments.component';

describe('TableDepartmentsComponent', () => {
  let component: TableDepartmentsComponent;
  let fixture: ComponentFixture<TableDepartmentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableDepartmentsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableDepartmentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
