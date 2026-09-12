import { TestBed } from '@angular/core/testing';

import { UnitStateService } from './unit-state.service';

describe('UnitStateService', () => {
  let service: UnitStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UnitStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
