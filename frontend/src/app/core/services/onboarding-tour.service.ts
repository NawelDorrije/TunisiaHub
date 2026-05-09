import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

export interface TourStep {
  selector: string;
  title: string;
  text: string;
  position: 'top' | 'bottom' | 'left' | 'right';
}

@Injectable({
  providedIn: 'root'
})
export class OnboardingTourService {
  private readonly STORAGE_KEY = 'tunisiahub_tour_done';
  
  private steps: TourStep[] = [
    {
      selector: '[data-tour="my-shop"]',
      title: 'Welcome to Your Shop!',
      text: 'This is where you can manage your shop details, products, and see how customers view your page.',
      position: 'bottom'
    },
    {
      selector: '[data-tour="promote-btn"]',
      title: 'Boost Your Sales',
      text: 'Use our AI-powered tool to generate beautiful social media captions and promotional images in seconds.',
      position: 'left'
    },
    {
      selector: '[data-tour="orders"]',
      title: 'Manage Your Orders',
      text: 'Track all your incoming orders, update their status, and keep your customers informed.',
      position: 'top'
    },
    {
      selector: '[data-tour="ai-insights"]',
      title: 'AI-Powered Insights',
      text: 'Get deep insights into your reviews and products using our advanced AI analysis.',
      position: 'top'
    }
  ];

  private currentStepSubject = new BehaviorSubject<number>(-1);
  currentStep$ = this.currentStepSubject.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  startIfNeeded(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    
    const isDone = localStorage.getItem(this.STORAGE_KEY);
    if (!isDone) {
      setTimeout(() => {
        this.currentStepSubject.next(0);
      }, 800);
    }
  }

  next(): void {
    const current = this.currentStepSubject.value;
    if (current < this.steps.length - 1) {
      this.currentStepSubject.next(current + 1);
    } else {
      this.finish();
    }
  }

  skip(): void {
    this.finish();
  }

  finish(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.STORAGE_KEY, 'true');
    }
    this.currentStepSubject.next(-1);
  }

  getStep(index: number): TourStep | null {
    if (index >= 0 && index < this.steps.length) {
      return this.steps[index];
    }
    return null;
  }

  get totalSteps(): number {
    return this.steps.length;
  }
}
