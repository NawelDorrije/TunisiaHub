export interface AiSearchRequest {
  query: string;
  latitude?: number;
  longitude?: number;
}

export interface AiSearchResponse {
  results: any[];
  summary?: string;
}

export interface AiSearchError {
  message: string;
  code?: number;
}
