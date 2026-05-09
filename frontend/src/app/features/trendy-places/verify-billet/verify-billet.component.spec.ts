import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyBilletComponent } from './verify-billet.component';

describe('VerifyBilletComponent', () => {
  let component: VerifyBilletComponent;
  let fixture: ComponentFixture<VerifyBilletComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VerifyBilletComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerifyBilletComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
