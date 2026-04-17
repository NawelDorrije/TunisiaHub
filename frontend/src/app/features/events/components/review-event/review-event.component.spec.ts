import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewEventComponent } from './review-event.component';

describe('ReviewEventComponent', () => {
  let component: ReviewEventComponent;
  let fixture: ComponentFixture<ReviewEventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReviewEventComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewEventComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
