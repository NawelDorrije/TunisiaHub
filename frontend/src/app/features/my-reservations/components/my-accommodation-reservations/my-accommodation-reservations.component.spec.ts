import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAccommodationReservationsComponent } from './my-accommodation-reservations.component';

describe('MyAccommodationReservationsComponent', () => {
  let component: MyAccommodationReservationsComponent;
  let fixture: ComponentFixture<MyAccommodationReservationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyAccommodationReservationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyAccommodationReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
