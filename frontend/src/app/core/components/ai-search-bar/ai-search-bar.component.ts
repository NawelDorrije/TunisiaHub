import { ChangeDetectionStrategy, Component, HostListener, inject, output, signal, PLATFORM_ID, OnInit } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiSearchService } from '../../../services/ai-search.service';

@Component({
  selector: 'app-ai-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-search-bar.component.html',
  styleUrls: ['./ai-search-bar.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AiSearchBarComponent implements OnInit {
  // Injecting AiSearchService (Angular 18 approach)
  private aiSearchService = inject(AiSearchService);
  private platformId = inject(PLATFORM_ID);

  // Signals for local state management (synced with service)
  searchQuery = signal<string>('');
  isLoading = this.aiSearchService.isLoading;
  isListening = signal<boolean>(false);
  error = this.aiSearchService.error;

  // Output signal (new in Angular 17.3+)
  searchTriggered = output<string>();

  private recognition: any;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.initVoiceRecognition();
    }
  }

  /**
   * Initializes the Web Speech API recognition object.
   */
  private initVoiceRecognition(): void {
    if (typeof window === 'undefined') return;
    
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US'; // Default to English, can be dynamic

      this.recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        this.searchQuery.set(transcript);
        this.isListening.set(false);
      };

      this.recognition.onerror = (event: any) => {
        console.error('Speech recognition error:', event.error);
        this.isListening.set(false);
      };

      this.recognition.onend = () => {
        this.isListening.set(false);
      };
    }
  }

  /**
   * Toggles the voice listening state.
   */
  toggleListening(): void {
    if (!this.recognition) {
      alert('Speech recognition is not supported in this browser.');
      return;
    }

    if (this.isListening()) {
      this.recognition.stop();
      this.isListening.set(false);
    } else {
      this.isListening.set(true);
      this.recognition.start();
    }
  }

  /**
   * Handles the search logic when the button is clicked or Enter is pressed.
   */
  onSearch(): void {
    const query = this.searchQuery().trim();
    if (query) {
      this.isLoading.set(true);
      this.searchTriggered.emit(query);

      // Simulate an AI search delay for visualization
      setTimeout(() => {
        this.isLoading.set(false);
      }, 2000);
    }
  }

  /**
   * Handles keyboard Enter key to trigger search.
   */
  @HostListener('keydown.enter')
  onEnter(): void {
    this.onSearch();
  }
}
