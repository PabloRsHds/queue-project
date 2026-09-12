import { TestBed } from '@angular/core/testing';

import { DepartmentStateService } from './department-state.service';

describe('DepartmentStateService', () => {
  let service: DepartmentStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DepartmentStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
