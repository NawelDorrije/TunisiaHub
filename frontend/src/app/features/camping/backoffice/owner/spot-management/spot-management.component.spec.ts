import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SpotManagementComponent } from './spot-management.component';

describe('SpotManagementComponent', () => {
  let component: SpotManagementComponent;
  let fixture: ComponentFixture<SpotManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SpotManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SpotManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
