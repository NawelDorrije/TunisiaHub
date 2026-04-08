import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaiementReservationComponent } from './paiement-reservation.component';

describe('PaiementReservationComponent', () => {
  let component: PaiementReservationComponent;
  let fixture: ComponentFixture<PaiementReservationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PaiementReservationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaiementReservationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
