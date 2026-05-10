import {
  Component,
  ElementRef,
  Inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { EventService } from '../../services/event.service';
import { AuthService } from '../../../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { CalendarOptions } from '@fullcalendar/core';
import { calendarPlugins } from '../../../../shared/config/calendar.config';
import { Event } from '../../../../models/events/event.model';
import { AiEventChatResponse } from '../../../../models/events/ai-event-chat.model';
import {
  WeatherDTO,
  ForecastResponse,
  ForecastItem
} from '../../../../models/weather.model';

export interface DayForecast {
  day: string;
  date: string;
  dateObj: Date;
  tempHi: number;
  tempLo: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  description: string;
  icon: 'sun' | 'cloud' | 'rain' | 'snow' | 'storm';
  iconCode: string;
  barPct: number;
}

interface ChatMessage {
  role: 'assistant' | 'user';
  text: string;
  events?: Event[];
  createdAt: Date;
}

@Component({
  selector: 'app-list-events-user',
  templateUrl: './list-events-user.component.html',
  styleUrls: ['./list-events-user.component.css']
})
export class ListEventsUserComponent implements OnInit, OnDestroy {
  @ViewChild('chatScrollContainer') chatScrollContainer?: ElementRef<HTMLDivElement>;

  events: Event[] = [];
  filteredEvents: Event[] = [];
  searchQuery = '';

  showPopup = false;
  errorMessage = '';

  notifications: string[] = [];
  private notificationSub!: Subscription;

  isChatOpen = false;
  aiPrompt = '';
  aiLoading = false;
  chatMessages: ChatMessage[] = [];
  readonly suggestedPrompts = [
    'Show me outdoor events',
    'I want sport events',
    'Montre-moi des evenements en nature',
    'Music events near me'
  ];

  // Calendar popup
  showCalendarPopup = false;
  selectedCalendarDate: Date | null = null;
  selectedDayEvents: Event[] = [];
  selectedEventDetail: Event | null = null;
  currentCalDate: Date = new Date();
  calendarCells: {
    date: Date | null;
    day: number | null;
    isCurrentMonth: boolean;
    isToday: boolean;
    events: Event[];
  }[] = [];
  readonly dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  // Weather
  todayWeather: WeatherDTO | null = null;
  weekForecast: DayForecast[] = [];
  selectedDay: DayForecast | null = null;
  weatherLoading = false;
  weatherError = false;

  private readonly defaultLat = 36.8065;
  private readonly defaultLon = 10.1815;

  calendarOptions: CalendarOptions = {
    plugins: calendarPlugins,
    initialView: 'dayGridMonth',
    height: 'auto',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    eventContent: (arg) => ({
      html: `<div class="fc-ev-inner">
               <span class="fc-ev-dot"></span>
               <span class="fc-ev-title">${arg.event.title}</span>
             </div>`
    }),
    eventClick: (info) => {
      this.router.navigate(['/events/details', info.event.id], {
        state: { returnUrl: '/events/user/events' }
      });
    }
  };

  constructor(
    private eventService: EventService,
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.seedAssistantGreeting();

    if (isPlatformBrowser(this.platformId)) {
      this.loadEvents();
      this.loadWeather();

      this.notificationSub = this.notificationService
        .connect()
        .subscribe((msg: string) => {
          this.showToast(msg);
        });
    }
  }

  ngOnDestroy(): void {
    if (this.notificationSub) {
      this.notificationSub.unsubscribe();
    }

    this.notificationService.disconnect();
  }

  showToast(message: string): void {
    this.notifications.push(message);

    setTimeout(() => {
      this.notifications.shift();
    }, 5000);
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe((data) => {
      this.events = data;
      this.filteredEvents = data;
      this.buildCalendar();
    });
  }

  filterEvents(): void {
    const q = this.searchQuery.toLowerCase().trim();

    if (!q) {
      this.filteredEvents = this.events;
      return;
    }

    this.filteredEvents = this.events.filter((event) => {
      const titleMatch = event.title?.toLowerCase().includes(q);
      const lieuMatch = event.lieu?.toLowerCase().includes(q);
      const descMatch = event.description?.toLowerCase().includes(q);
      const typeMatch = event.type?.toLowerCase().includes(q);
      const priceMatch = event.price?.toString().includes(q);

      return titleMatch || lieuMatch || descMatch || typeMatch || priceMatch;
    });
  }

  isCompleted(event: Event): boolean {
    return event.status === 'COMPLETED';
  }

  getStatusClass(status: string): string {
    return status === 'COMPLETED' ? 's-c' : 's-a';
  }

  goToDetails(id: number | undefined): void {
    if (!id) {
      return;
    }

    this.router.navigate(['/events/details', id], {
      state: { returnUrl: '/events/user/events' }
    });
  }

  reserveEvent(event: Event): void {
    if (this.isCompleted(event) || !event.id) return;

    const userId = this.authService.getUserId();
    this.eventService.reserveEvent(userId, event.id).subscribe({
      next: () => this.router.navigate(['/events/reserve', event.id]),
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred. Please try again.';
        this.showPopup = true;
      }
    });
  }

  closePopup(): void {
    this.showPopup = false;
  }

  toggleChat(): void {
    this.isChatOpen = !this.isChatOpen;
    this.scheduleChatScroll();
  }

  useSuggestedPrompt(prompt: string): void {
    this.aiPrompt = prompt;
    this.sendAiMessage();
  }

  sendAiMessage(): void {
    const message = this.aiPrompt.trim();

    if (!message || this.aiLoading) {
      return;
    }

    this.chatMessages.push({
      role: 'user',
      text: message,
      createdAt: new Date()
    });

    this.aiPrompt = '';
    this.aiLoading = true;
    this.scheduleChatScroll();

    this.eventService.chatWithAi(message).subscribe({
      next: (response: AiEventChatResponse) => {
        this.chatMessages.push({
          role: 'assistant',
          text: response.message,
          events: response.events,
          createdAt: new Date()
        });
        this.aiLoading = false;
        this.scheduleChatScroll();
      },
      error: () => {
        this.aiLoading = false;
        this.scheduleChatScroll();
      }
    });
  }

  trackByEventId(_: number, event: Event): number | string {
    return event.id ?? event.title;
  }

  getEventCardBackgroundImage(image?: string): string {
    const candidate = (image ?? '').trim();
    if (!candidate) {
      return '';
    }
    return `url('${encodeURI(candidate).replace(/'/g, '%27')}')`;
  }

  getChatEventDescription(description: string | undefined, maxLength = 115): string {
    if (!description) {
      return 'Description available on the event details page.';
    }

    return description.length > maxLength
      ? `${description.slice(0, maxLength)}...`
      : description;
  }

  getChatEventDate(date: string | undefined): string {
    if (!date) {
      return 'Date available on details';
    }

    return new Date(date).toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  get currentMonthLabel(): string {
    return this.currentCalDate.toLocaleDateString('en-GB', {
      month: 'long',
      year: 'numeric'
    });
  }

  openCalendar(): void {
    this.showCalendarPopup = true;
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
    this.buildCalendar();
  }

  closeCalendar(): void {
    this.showCalendarPopup = false;
  }

  buildCalendar(): void {
    const year = this.currentCalDate.getFullYear();
    const month = this.currentCalDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const today = new Date();

    let startDow = firstDay.getDay();
    startDow = startDow === 0 ? 6 : startDow - 1;

    const start = new Date(firstDay);
    start.setDate(start.getDate() - startDow);

    this.calendarCells = [];
    const cur = new Date(start);

    for (let i = 0; i < 42; i++) {
      const isCurrent = cur.getMonth() === month;
      const isToday = cur.toDateString() === today.toDateString();
      const cellDate = new Date(cur);
      const cellEvents = this.events.filter(
        (event) =>
          event.startDate && new Date(event.startDate).toDateString() === cellDate.toDateString()
      );

      this.calendarCells.push({
        date: cellDate,
        day: isCurrent ? cur.getDate() : null,
        isCurrentMonth: isCurrent,
        isToday,
        events: cellEvents
      });
      cur.setDate(cur.getDate() + 1);
    }
  }

  prevMonth(): void {
    this.currentCalDate = new Date(
      this.currentCalDate.getFullYear(),
      this.currentCalDate.getMonth() - 1,
      1
    );
    this.buildCalendar();
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
  }

  nextMonth(): void {
    this.currentCalDate = new Date(
      this.currentCalDate.getFullYear(),
      this.currentCalDate.getMonth() + 1,
      1
    );
    this.buildCalendar();
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
  }

  goToToday(): void {
    this.currentCalDate = new Date();
    this.buildCalendar();
  }

  selectCalDay(cell: { date: Date | null; events: Event[] }): void {
    if (!cell.date) return;
    this.selectedCalendarDate = cell.date;
    this.selectedDayEvents = cell.events;
    this.selectedEventDetail = null;
  }

  selectEventDetail(event: Event): void {
    this.selectedEventDetail = event;
  }

  isSameDay(a: Date, b: Date): boolean {
    return a.toDateString() === b.toDateString();
  }

  loadWeather(): void {
    this.weatherLoading = true;
    this.weatherError = false;

    this.eventService.getWeather(this.defaultLat, this.defaultLon).subscribe({
      next: (weather: WeatherDTO) => {
        this.todayWeather = weather;
      },
      error: () => {}
    });

    this.eventService.getWeeklyForecast(this.defaultLat, this.defaultLon).subscribe({
      next: (res: ForecastResponse) => {
        this.weekForecast = this.parseForecast(res);
        this.selectedDay = this.weekForecast[0] ?? null;
        this.weatherLoading = false;
      },
      error: () => {
        this.weatherError = true;
        this.weatherLoading = false;
      }
    });
  }

  selectDay(day: DayForecast): void {
    this.selectedDay = day;
  }

  private parseForecast(res: ForecastResponse): DayForecast[] {
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    const grouped: Record<string, ForecastItem[]> = {};

    res.list.forEach((item) => {
      const key = item.dt_txt.slice(0, 10);
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(item);
    });

    return Object.entries(grouped)
      .slice(0, 10)
      .map(([dateKey, items], index) => {
        const date = new Date(dateKey);
        const temps = items.map((entry) => entry.main.temp);
        const tempHi = Math.round(Math.max(...temps));
        const tempLo = Math.round(Math.min(...temps));
        const rep = items.find((entry) => entry.dt_txt.includes('12:00')) ?? items[0];
        const iconCode = rep.weather[0]?.icon ?? '01d';
        const mainWeather = rep.weather[0]?.main?.toLowerCase() ?? '';

        let icon: DayForecast['icon'] = 'sun';
        if (mainWeather.includes('snow')) icon = 'snow';
        else if (mainWeather.includes('thunder')) icon = 'storm';
        else if (mainWeather.includes('rain') || mainWeather.includes('drizzle')) icon = 'rain';
        else if (mainWeather.includes('cloud')) icon = 'cloud';

        const barPct = Math.min(100, Math.max(5, Math.round((tempHi / 45) * 100)));

        return {
          day: index === 0 ? 'Today' : index === 1 ? 'Tomorrow' : dayNames[date.getDay()],
          date: date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }),
          dateObj: date,
          tempHi,
          tempLo,
          feelsLike: Math.round(rep.main.feels_like),
          humidity: rep.main.humidity,
          windSpeed: Math.round(rep.wind.speed),
          description: rep.weather[0]?.description ?? '',
          icon,
          iconCode,
          barPct
        };
      });
  }

  private seedAssistantGreeting(): void {
    if (this.chatMessages.length > 0) {
      return;
    }

    this.chatMessages = [
      {
        role: 'assistant',
        text: 'Ask me for real events from the database, like "show me outdoor events" or "montre-moi des evenements sportifs".',
        createdAt: new Date()
      }
    ];
  }

  private scheduleChatScroll(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    setTimeout(() => {
      const container = this.chatScrollContainer?.nativeElement;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 0);
  }
}
