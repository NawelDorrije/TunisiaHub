import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAccommodationComponent } from './add-accommodation.component';

describe('AddAccommodationComponent', () => {
  let component: AddAccommodationComponent;
  let fixture: ComponentFixture<AddAccommodationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddAccommodationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddAccommodationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
