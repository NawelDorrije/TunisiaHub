package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.example.backend_tunisiahub.dto.event.EventSlotRecommendationRequest;
import org.example.backend_tunisiahub.dto.event.EventSlotRecommendationResponse;
import org.example.backend_tunisiahub.dto.event.RecommendedSlotDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EventSlotRecommendationService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<LocalTime> CANDIDATE_TIMES = List.of(
            LocalTime.of(10, 0),
            LocalTime.of(14, 0),
            LocalTime.of(18, 0),
            LocalTime.of(20, 0)
    );

    private final EventRepository eventRepository;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${event.recommendation.weekend-score:30}")
    private int weekendScore;
    @Value("${event.recommendation.evening-score:25}")
    private int eveningScore;
    @Value("${event.recommendation.afternoon-score:15}")
    private int afternoonScore;
    @Value("${event.recommendation.morning-score:5}")
    private int morningScore;
    @Value("${event.recommendation.weekday-morning-penalty:10}")
    private int weekdayMorningPenalty;
    @Value("${event.recommendation.students-evening-score:20}")
    private int studentsEveningScore;
    @Value("${event.recommendation.students-weekend-score:15}")
    private int studentsWeekendScore;
    @Value("${event.recommendation.professionals-after18-score:22}")
    private int professionalsAfter18Score;
    @Value("${event.recommendation.professionals-weekend-score:8}")
    private int professionalsWeekendScore;
    @Value("${event.recommendation.general-weekend-score:8}")
    private int generalWeekendScore;
    @Value("${event.recommendation.competition-per-event:8}")
    private int competitionPerEvent;
    @Value("${event.recommendation.max-competition-penalty:40}")
    private int maxCompetitionPenalty;
    @Value("${event.recommendation.cache-ttl-seconds:600}")
    private long cacheTtlSeconds;

    public EventSlotRecommendationResponse recommendSlots(EventSlotRecommendationRequest input) {
        LocalDate date = LocalDate.parse(input.getDate());
        LocalTime time = LocalTime.parse(input.getTime().length() == 5 ? input.getTime() : input.getTime().substring(0, 5));
        LocalDateTime selectedDateTime = LocalDateTime.of(date, time);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime referenceDateTime = selectedDateTime.isAfter(now) ? selectedDateTime : now;
        String audience = normalizeAudience(input.getTarget_audience());

        String cacheKey = buildCacheKey(date, time, input.getType(), audience);
        EventSlotRecommendationResponse cached = getFromCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        boolean hasDbEvents = eventRepository.count() > 0;
        SlotScore original = scoreSlot(date, time, audience, hasDbEvents);
        List<LocalTime> candidateTimes = buildCandidateTimes(time);

        List<SlotScore> candidates = new ArrayList<>();
        for (int dayOffset = 0; dayOffset <= 3; dayOffset++) {
            LocalDate candidateDate = referenceDateTime.toLocalDate().plusDays(dayOffset);
            for (LocalTime candidateTime : candidateTimes) {
                LocalDateTime candidateDateTime = LocalDateTime.of(candidateDate, candidateTime);
                if (!candidateDateTime.isAfter(referenceDateTime)) {
                    continue;
                }
                if (candidateDate.equals(date) && candidateTime.equals(time)) {
                    continue;
                }
                candidates.add(scoreSlot(candidateDate, candidateTime, audience, hasDbEvents));
            }
        }

        List<RecommendedSlotDto> top3 = candidates.stream()
                .sorted(Comparator
                        .comparingInt(SlotScore::total).reversed()
                        .thenComparing(SlotScore::date)
                        .thenComparing(SlotScore::time))
                .limit(3)
                .map(slot -> new RecommendedSlotDto(
                        slot.date().toString(),
                        slot.time().format(TIME_FMT),
                        slot.total(),
                        engagementLabel(slot.total()),
                        buildReason(slot),
                        calculateImprovementPercent(original.total(), slot.total())
                ))
                .toList();

        EventSlotRecommendationResponse response = new EventSlotRecommendationResponse(
                original.total(),
                engagementLabel(original.total()),
                buildReason(original),
                top3
        );

        putInCache(cacheKey, response);
        return response;
    }

    private List<LocalTime> buildCandidateTimes(LocalTime selectedTime) {
        Set<LocalTime> unique = new LinkedHashSet<>(CANDIDATE_TIMES);
        LocalTime plusOneHour = selectedTime.plusHours(1).withMinute(0);
        LocalTime minusOneHour = selectedTime.minusHours(1).withMinute(0);

        unique.add(minusOneHour);
        unique.add(plusOneHour);
        return new ArrayList<>(unique);
    }

    private SlotScore scoreSlot(LocalDate date, LocalTime time, String audience, boolean hasDbEvents) {
        int dayScore = isWeekend(date) ? weekendScore : 0;
        int hourScore = scoreHour(date, time);
        int audienceScore = scoreAudience(date, time, audience);
        int competitionScore = scoreCompetition(date, hasDbEvents);

        // Requested formula:
        // score = day_score + hour_score + audience_score - competition_score
        int total = Math.max(0, dayScore + hourScore + audienceScore - competitionScore);
        return new SlotScore(date, time, total, dayScore, hourScore, audienceScore, competitionScore);
    }

    private int scoreHour(LocalDate date, LocalTime time) {
        int hour = time.getHour();
        int score;
        if (hour >= 18 && hour <= 21) {
            score = eveningScore;
        } else if (hour >= 14 && hour <= 17) {
            score = afternoonScore;
        } else {
            score = morningScore;
        }

        boolean isWeekdayMorning = !isWeekend(date) && hour < 12;
        if (isWeekdayMorning) {
            score -= weekdayMorningPenalty;
        }
        return Math.max(0, score);
    }

    private int scoreAudience(LocalDate date, LocalTime time, String audience) {
        int hour = time.getHour();
        if ("students".equals(audience)) {
            int score = 0;
            if (hour >= 18) {
                score += studentsEveningScore;
            }
            if (isWeekend(date)) {
                score += studentsWeekendScore;
            }
            return score;
        }

        if ("professionals".equals(audience)) {
            if (hour >= 18) {
                return professionalsAfter18Score;
            }
            if (isWeekend(date)) {
                return professionalsWeekendScore;
            }
            return 0;
        }

        // No explicit audience selected: keep audience weight neutral.
        return 0;
    }

    private int scoreCompetition(LocalDate date, boolean hasDbEvents) {
        long dailyCount;
        if (hasDbEvents) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            dailyCount = eventRepository.countByStartDateBetween(dayStart, dayEnd);
        } else {
            // Bonus fallback: when DB is empty, keep deterministic mock competition.
            dailyCount = isWeekend(date) ? 1 : 0;
        }

        long penalty = dailyCount * competitionPerEvent;
        return (int) Math.min(maxCompetitionPenalty, penalty);
    }

    private int calculateImprovementPercent(int original, int candidate) {
        if (original <= 0) {
            return candidate > 0 ? 100 : 0;
        }
        double improvement = ((double) (candidate - original) / (double) original) * 100.0;
        return (int) Math.round(improvement);
    }

    private String buildReason(SlotScore slot) {
        List<String> reasons = new ArrayList<>();
        if (slot.dayScore() > 0) {
            reasons.add("weekend boost");
        }
        if (slot.hourScore() >= eveningScore) {
            reasons.add("high evening activity");
        } else if (slot.hourScore() >= afternoonScore) {
            reasons.add("solid afternoon traffic");
        } else {
            reasons.add("low daytime traffic");
        }
        if (slot.audienceScore() > 0) {
            reasons.add("audience match");
        }
        if (slot.competitionScore() > 0) {
            reasons.add("competition penalty -" + slot.competitionScore());
        } else {
            reasons.add("low competition");
        }
        return String.join(", ", reasons);
    }

    private String engagementLabel(int score) {
        if (score >= 60) {
            return "High engagement";
        }
        if (score >= 35) {
            return "Medium engagement";
        }
        return "Low engagement";
    }

    private String normalizeAudience(String audience) {
        String value = Objects.toString(audience, "").trim().toLowerCase(Locale.ROOT);
        if (value.equals("student")) {
            return "students";
        }
        if (value.equals("professional")) {
            return "professionals";
        }
        return value.isBlank() ? "auto" : value;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private String buildCacheKey(LocalDate date, LocalTime time, String type, String audience) {
        return date + "|" + time + "|" + Objects.toString(type, "") + "|" + audience;
    }

    private EventSlotRecommendationResponse getFromCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (entry.expireAtEpochMillis() < System.currentTimeMillis()) {
            cache.remove(key);
            return null;
        }
        return entry.response();
    }

    private void putInCache(String key, EventSlotRecommendationResponse response) {
        long ttlMillis = Math.max(1L, cacheTtlSeconds) * 1000L;
        cache.put(key, new CacheEntry(response, System.currentTimeMillis() + ttlMillis));
    }

    private record SlotScore(
            LocalDate date,
            LocalTime time,
            int total,
            int dayScore,
            int hourScore,
            int audienceScore,
            int competitionScore
    ) {
    }

    private record CacheEntry(EventSlotRecommendationResponse response, long expireAtEpochMillis) {
    }
}
