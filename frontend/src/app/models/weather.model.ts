export interface WeatherDTO {
  temperature: number;
  windSpeed: number;
  humidity: number;
  precipitation?: number;
  description?: string;
}

export interface ForecastItem {
  dt: number;
  main: {
    temp: number;
    temp_min: number;
    temp_max: number;
    humidity: number;
    feels_like: number;
  };
  weather: { main: string; description: string; icon: string }[];
  wind: { speed: number };
  dt_txt: string;
}

export interface ForecastResponse {
  list: ForecastItem[];
  city: { name: string; country: string };
}