import { Component, OnInit, OnDestroy, ChangeDetectorRef, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';
import { OnboardingTourService, TourStep } from '../../services/onboarding-tour.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-tour-overlay',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tour-overlay.component.html',
  styleUrls: ['./tour-overlay.component.css']
})
export class TourOverlayComponent implements OnInit, OnDestroy {
  currentStep = -1;
  step: TourStep | null = null;
  private sub?: Subscription;

  targetRect = { top: 0, left: 0, width: 0, height: 0 };
  tooltipPos = { top: 0, left: 0 };
  backdropShadow = '0 0 0 9999px rgba(0,0,0,0)';

  constructor(
    private tourService: OnboardingTourService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.sub = this.tourService.currentStep$.subscribe(stepIndex => {
      this.currentStep = stepIndex;
      if (stepIndex !== -1) {
        this.step = this.tourService.getStep(stepIndex);
        this.updatePosition();
      } else {
        this.step = null;
        this.backdropShadow = '0 0 0 9999px rgba(0,0,0,0)';
      }
      this.cdr.detectChanges();
    });

    if (isPlatformBrowser(this.platformId)) {
      window.addEventListener('resize', () => this.updatePosition());
      window.addEventListener('scroll', () => this.updatePosition(), true);
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    if (isPlatformBrowser(this.platformId)) {
      window.removeEventListener('resize', () => this.updatePosition());
      window.removeEventListener('scroll', () => this.updatePosition(), true);
    }
  }

  private updatePosition(): void {
    if (!this.step || !isPlatformBrowser(this.platformId)) return;

    const element = document.querySelector(this.step.selector);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // Wait for scroll to settle
      setTimeout(() => {
        const rect = element.getBoundingClientRect();
        this.targetRect = {
          top: rect.top - 8,
          left: rect.left - 8,
          width: rect.width + 16,
          height: rect.height + 16
        };

        this.calculateTooltipPosition(this.targetRect);
        this.backdropShadow = `0 0 0 9999px rgba(0,0,0,0.65)`;
        this.cdr.detectChanges();
      }, 300);
    } else {
      // If element not found on current page, maybe wait or skip?
      // For this demo, we'll just hide the spotlight but keep the backdrop
      this.targetRect = { top: -100, left: -100, width: 0, height: 0 };
      this.backdropShadow = `0 0 0 9999px rgba(0,0,0,0.65)`;
    }
  }

  private calculateTooltipPosition(target: any): void {
    if (!this.step) return;

    const padding = 20;
    const tooltipWidth = 320;
    const tooltipHeight = 200; // Estimated

    let top = 0;
    let left = 0;

    switch (this.step.position) {
      case 'bottom':
        top = target.top + target.height + padding;
        left = target.left + (target.width / 2) - (tooltipWidth / 2);
        break;
      case 'top':
        top = target.top - tooltipHeight - padding;
        left = target.left + (target.width / 2) - (tooltipWidth / 2);
        break;
      case 'left':
        top = target.top + (target.height / 2) - (tooltipHeight / 2);
        left = target.left - tooltipWidth - padding;
        break;
      case 'right':
        top = target.top + (target.height / 2) - (tooltipHeight / 2);
        left = target.left + target.width + padding;
        break;
    }

    // Viewport clamping
    const vWidth = isPlatformBrowser(this.platformId) ? window.innerWidth : 1200;
    const vHeight = isPlatformBrowser(this.platformId) ? window.innerHeight : 800;

    if (left < 10) left = 10;
    if (left + tooltipWidth > vWidth - 10) left = vWidth - tooltipWidth - 10;
    if (top < 10) top = 10;
    if (top + tooltipHeight > vHeight - 10) top = vHeight - tooltipHeight - 10;

    this.tooltipPos = { top, left };
  }

  next(): void {
    this.tourService.next();
  }

  skip(): void {
    this.tourService.skip();
  }

  get isLastStep(): boolean {
    return this.currentStep === this.tourService.totalSteps - 1;
  }
}
