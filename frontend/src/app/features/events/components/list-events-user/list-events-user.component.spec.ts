import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListEventsUserComponent } from './list-events-user.component';

describe('ListEventsUserComponent', () => {
  let component: ListEventsUserComponent;
  let fixture: ComponentFixture<ListEventsUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ListEventsUserComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListEventsUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
