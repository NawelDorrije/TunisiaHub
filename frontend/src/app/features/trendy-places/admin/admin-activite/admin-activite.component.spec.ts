import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminActiviteComponent } from './admin-activite.component';

describe('AdminActiviteComponent', () => {
  let component: AdminActiviteComponent;
  let fixture: ComponentFixture<AdminActiviteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminActiviteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminActiviteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
