import { TestBed } from '@angular/core/testing';

import { CustomerStateService } from './customer-state.service';

describe('CustomerStateService', () => {
  let service: CustomerStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CustomerStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
