import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private socket!: WebSocket;
  private messageSubject = new Subject<string>();

  connect(): Observable<string> {

    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {

      this.socket = new WebSocket('ws://localhost:8089/ws');

      this.socket.onopen = () => {
        console.log('✅ WebSocket connected');
      };

      this.socket.onmessage = (event) => {
        this.messageSubject.next(event.data);
      };

      this.socket.onerror = (err) => {
        console.error('❌ WebSocket error', err);
      };

      this.socket.onclose = () => {
        console.log('🔌 WebSocket closed');
      };
    }

    return this.messageSubject.asObservable();
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
    }
  }
}