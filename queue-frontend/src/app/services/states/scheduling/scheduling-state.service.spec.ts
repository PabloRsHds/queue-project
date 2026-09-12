import { TestBed } from '@angular/core/testing';

import { SchedulingStateService } from './scheduling-state.service';

describe('SchedulingStateService', () => {
  let service: SchedulingStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SchedulingStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
