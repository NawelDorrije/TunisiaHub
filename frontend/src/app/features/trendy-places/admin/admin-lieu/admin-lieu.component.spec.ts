import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminLieuComponent } from './admin-lieu.component';

describe('AdminLieuComponent', () => {
  let component: AdminLieuComponent;
  let fixture: ComponentFixture<AdminLieuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminLieuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminLieuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
