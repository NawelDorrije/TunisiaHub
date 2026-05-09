import { Event } from './event.model';

export interface AiEventChatRequest {
  message: string;
}

export interface AiEventChatResponse {
  message: string;
  events: Event[];
}
