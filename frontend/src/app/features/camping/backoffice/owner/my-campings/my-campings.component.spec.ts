import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyCampingsComponent } from './my-campings.component';

describe('MyCampingsComponent', () => {
  let component: MyCampingsComponent;
  let fixture: ComponentFixture<MyCampingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyCampingsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyCampingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
