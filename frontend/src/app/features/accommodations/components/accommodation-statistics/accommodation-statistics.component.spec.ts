import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccommodationStatisticsComponent } from './accommodation-statistics.component';

describe('AccommodationStatisticsComponent', () => {
  let component: AccommodationStatisticsComponent;
  let fixture: ComponentFixture<AccommodationStatisticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AccommodationStatisticsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccommodationStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
