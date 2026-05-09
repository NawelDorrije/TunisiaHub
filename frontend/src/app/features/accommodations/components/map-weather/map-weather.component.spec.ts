import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapWeatherComponent } from './map-weather.component';

describe('MapWeatherComponent', () => {
  let component: MapWeatherComponent;
  let fixture: ComponentFixture<MapWeatherComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MapWeatherComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MapWeatherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
