import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampingManagementComponent } from './camping-management.component';

describe('CampingManagementComponent', () => {
  let component: CampingManagementComponent;
  let fixture: ComponentFixture<CampingManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampingManagementComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CampingManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
