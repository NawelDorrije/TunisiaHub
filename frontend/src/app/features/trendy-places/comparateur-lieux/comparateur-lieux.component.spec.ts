import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComparateurLieuxComponent } from './comparateur-lieux.component';

describe('ComparateurLieuxComponent', () => {
  let component: ComparateurLieuxComponent;
  let fixture: ComponentFixture<ComparateurLieuxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ComparateurLieuxComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComparateurLieuxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
