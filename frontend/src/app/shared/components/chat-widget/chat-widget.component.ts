import { Component, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormControl } from '@angular/forms';

interface Message {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Component({
  selector: 'app-chat-widget',
  templateUrl: './chat-widget.component.html',
  styleUrls: ['./chat-widget.component.css']
})
export class ChatWidgetComponent {

  isOpen = false;
  isLoading = false;
  messages: Message[] = [
    {
      role: 'assistant',
      content: '👋 Hello! I\'m TunisiaHub\'s AI assistant. Ask me anything about accommodations in Tunisia!',
      timestamp: new Date()
    }
  ];

  messageInput = new FormControl('');
  sessionId = 'session_' + Math.random().toString(36).substr(2, 9);

  private apiUrl = 'http://localhost:8089/api/ai/chat';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  sendMessage(): void {
    const message = this.messageInput.value?.trim();
    if (!message || this.isLoading) return;

    this.messages.push({
      role: 'user',
      content: message,
      timestamp: new Date()
    });

    this.messageInput.setValue('');
    this.isLoading = true;

    this.http.post<any>(this.apiUrl, {
      message: message,
      session_id: this.sessionId
    }).subscribe({
      next: (response) => {
        this.messages.push({
          role: 'assistant',
          content: response.response,
          timestamp: new Date()
        });
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: () => {
        this.messages.push({
          role: 'assistant',
          content: '❌ Sorry, I\'m having trouble connecting. Please try again.',
          timestamp: new Date()
        });
        this.isLoading = false;
      }
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  scrollToBottom(): void {
    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        const container = document.getElementById('chat-messages');
        if (container) container.scrollTop = container.scrollHeight;
      }, 100);
    }
  }

  clearChat(): void {
    this.messages = [{
      role: 'assistant',
      content: '👋 Chat cleared! How can I help you?',
      timestamp: new Date()
    }];
  }
}