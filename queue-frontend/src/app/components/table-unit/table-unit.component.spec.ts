import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableUnitComponent } from './table-unit.component';

describe('TableUnitComponent', () => {
  let component: TableUnitComponent;
  let fixture: ComponentFixture<TableUnitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableUnitComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TableUnitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
