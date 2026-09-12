import { TestBed } from '@angular/core/testing';

import { AttendentStateService } from './attendent-state.service';

describe('AttendentStateService', () => {
  let service: AttendentStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AttendentStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
