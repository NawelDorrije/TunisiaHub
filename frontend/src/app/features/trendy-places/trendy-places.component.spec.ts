import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrendyPlacesComponent } from './trendy-places.component';

describe('TrendyPlacesComponent', () => {
  let component: TrendyPlacesComponent;
  let fixture: ComponentFixture<TrendyPlacesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TrendyPlacesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrendyPlacesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
