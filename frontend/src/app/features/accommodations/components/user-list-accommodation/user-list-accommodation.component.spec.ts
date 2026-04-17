import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserListAccommodationComponent } from './user-list-accommodation.component';

describe('UserListAccommodationComponent', () => {
  let component: UserListAccommodationComponent;
  let fixture: ComponentFixture<UserListAccommodationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserListAccommodationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserListAccommodationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
