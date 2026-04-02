import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminListAccommodationComponent } from './admin-list-accommodation.component';

describe('AdminListAccommodationComponent', () => {
  let component: AdminListAccommodationComponent;
  let fixture: ComponentFixture<AdminListAccommodationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminListAccommodationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminListAccommodationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
