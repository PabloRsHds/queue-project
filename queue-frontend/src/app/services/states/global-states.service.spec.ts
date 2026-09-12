import { TestBed } from '@angular/core/testing';

import { GlobalStatesService } from './global-states.service';

describe('GlobalStatesService', () => {
  let service: GlobalStatesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GlobalStatesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
