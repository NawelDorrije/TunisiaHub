import { TestBed } from '@angular/core/testing';

import { TrendyPlacesService } from './trendy-places.service';

describe('TrendyPlacesService', () => {
  let service: TrendyPlacesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TrendyPlacesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
