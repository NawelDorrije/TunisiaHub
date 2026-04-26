import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampingDetailComponent } from './camping-detail.component';

describe('CampingDetailComponent', () => {
  let component: CampingDetailComponent;
  let fixture: ComponentFixture<CampingDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampingDetailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CampingDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
