import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailsAccommodationComponent } from './details-accommodation.component';

describe('DetailsAccommodationComponent', () => {
  let component: DetailsAccommodationComponent;
  let fixture: ComponentFixture<DetailsAccommodationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DetailsAccommodationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailsAccommodationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
