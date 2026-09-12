import { TestBed } from '@angular/core/testing';

import { TicketStateService } from './ticket-state.service';

describe('TicketStateService', () => {
  let service: TicketStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TicketStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
