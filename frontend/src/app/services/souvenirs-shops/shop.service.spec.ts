import { TestBed } from '@angular/core/testing';

import { ShopService } from './shop.service';
import { describe, beforeEach, it } from 'node:test';

describe('ShopService', () => {
  let service: ShopService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ShopService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
