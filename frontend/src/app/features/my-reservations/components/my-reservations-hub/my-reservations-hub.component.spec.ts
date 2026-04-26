import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyReservationsHubComponent } from './my-reservations-hub.component';

describe('MyReservationsHubComponent', () => {
  let component: MyReservationsHubComponent;
  let fixture: ComponentFixture<MyReservationsHubComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyReservationsHubComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyReservationsHubComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
