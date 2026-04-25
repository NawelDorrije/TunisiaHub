package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.EventType;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.example.backend_tunisiahub.dto.event.AiEventChatResponse;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiEventRecommendationService {

    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
    private static final int MAX_RESULTS = 5;
    private static final Set<String> STOP_WORDS = orderedSet(
            "a", "an", "the", "i", "me", "my", "for", "with", "show", "need", "want", "looking",
            "find", "some", "any", "events", "event", "please", "near", "nearby", "around",
            "je", "veux", "voudrais", "cherche", "chercher", "des", "de", "du", "la", "le",
            "les", "un", "une", "pour", "avec", "moi", "pres", "proche", "autour", "dans",
            "je", "veux", "evenement", "evennements", "evennement"
    );
    private static final Map<String, ThemeDefinition> THEME_DEFINITIONS = buildThemeDefinitions();

    private final EventRepository eventRepository;

    public AiEventChatResponse recommendEvents(String message) {
        try {
            String normalizedMessage = normalize(message);
            if (normalizedMessage.isBlank()) {
                return emptyResponse("Tell me what kind of event you want, for example sport, conference, music or outdoor.");
            }

            QueryContext queryContext = buildQueryContext(normalizedMessage);
            List<ScoredEvent> scoredEvents = eventRepository.findAll().stream()
                    .map(event -> scoreEvent(event, queryContext))
                    .filter(scoredEvent -> scoredEvent.score() > 0)
                    .sorted(Comparator
                            .comparingInt(ScoredEvent::score).reversed()
                            .thenComparing((ScoredEvent scoredEvent) -> isUpcoming(scoredEvent.event()) ? 0 : 1)
                            .thenComparing(scoredEvent -> scoredEvent.event().getStartDate(),
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                    .limit(MAX_RESULTS)
                    .toList();

            List<Event> matchedEvents = scoredEvents.stream()
                    .map(ScoredEvent::event)
                    .toList();

            if (matchedEvents.isEmpty()) {
                return emptyResponse(buildNoMatchAnswer(message));
            }

            return new AiEventChatResponse(
                    buildSuccessAnswer(queryContext, matchedEvents.size()),
                    matchedEvents
            );
        } catch (Exception exception) {
            log.error("AI recommendation failure for message={}", message, exception);
            return emptyResponse("No events found or error handled safely");
        }
    }

    private QueryContext buildQueryContext(String normalizedMessage) {
        Set<String> keywords = new LinkedHashSet<>();
        Set<EventType> targetedTypes = EnumSet.noneOf(EventType.class);
        Set<String> matchedThemes = new LinkedHashSet<>();

        List<String> rawTokens = Arrays.stream(normalizedMessage.split(" "))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();

        for (Map.Entry<String, ThemeDefinition> entry : THEME_DEFINITIONS.entrySet()) {
            if (containsAny(normalizedMessage, entry.getValue().triggers())) {
                matchedThemes.add(entry.getKey());
                keywords.addAll(entry.getValue().keywords());
                targetedTypes.addAll(entry.getValue().eventTypes());
            }
        }

        for (String token : rawTokens) {
            if (!STOP_WORDS.contains(token) && token.length() > 1) {
                keywords.add(token);
            }
        }

        if (containsAny(normalizedMessage, List.of("music", "musique", "concert", "live"))) {
            matchedThemes.add("music");
        }
        if (containsAny(normalizedMessage, List.of("outdoor", "nature", "plein air"))) {
            matchedThemes.add("outdoor");
        }

        return new QueryContext(normalizedMessage, keywords, targetedTypes, matchedThemes);
    }

    private static Map<String, ThemeDefinition> buildThemeDefinitions() {
        Map<String, ThemeDefinition> themes = new LinkedHashMap<>();

        themes.put("sport", new ThemeDefinition(
                orderedSet("sport", "sports", "football", "gym", "fitness", "match", "running",
                        "soccer", "basketball", "tennis", "tournoi", "competition sportive",
                        "sportif", "sportive", "athletic"),
                orderedSet("sport", "sports", "football", "gym", "fitness", "match", "running",
                        "soccer", "basketball", "tennis", "stadium", "workout", "athletic"),
                EnumSet.of(EventType.SPORT, EventType.COMPETITION)
        ));

        themes.put("outdoor", new ThemeDefinition(
                orderedSet("outdoor", "outdoors", "nature", "natural", "plein air", "randonnee",
                        "hiking", "camping", "adventure", "forest", "beach", "park"),
                orderedSet("outdoor", "nature", "plein air", "hiking", "camping", "adventure",
                        "forest", "beach", "park", "mountain", "eco", "garden", "randonnee"),
                EnumSet.noneOf(EventType.class)
        ));

        themes.put("music", new ThemeDefinition(
                orderedSet("music", "musique", "concert", "dj", "festival", "live", "party", "show"),
                orderedSet("music", "musique", "concert", "dj", "festival", "live", "party", "show"),
                EnumSet.of(EventType.FESTIVAL)
        ));

        themes.put("conference", new ThemeDefinition(
                orderedSet("conference", "meetup", "networking", "business", "tech",
                        "atelier", "workshop", "seminar", "seminaire", "talk", "business event"),
                orderedSet("conference", "meetup", "networking", "business", "tech", "atelier",
                        "workshop", "seminar", "seminaire", "innovation", "talk", "business event"),
                EnumSet.of(EventType.CONFERENCE)
        ));

        themes.put("competition", new ThemeDefinition(
                orderedSet("competition", "contest", "challenge", "tournoi", "tournament", "race"),
                orderedSet("competition", "contest", "challenge", "tournoi", "tournament", "race"),
                EnumSet.of(EventType.COMPETITION, EventType.SPORT)
        ));

        return Collections.unmodifiableMap(themes);
    }

    private ScoredEvent scoreEvent(Event event, QueryContext queryContext) {
        String normalizedTitle = normalize(event.getTitle());
        String normalizedDescription = normalize(event.getDescription());
        String normalizedType = normalize(event.getType() != null ? event.getType().name() : "");
        String normalizedLieu = normalize(event.getLieu());

        int score = 0;

        for (String keyword : queryContext.keywords()) {
            if (normalizedTitle.contains(keyword)) {
                score += 3;
            }
            if (normalizedType.contains(keyword)) {
                score += 5;
            }
            if (normalizedDescription.contains(keyword)) {
                score += 2;
            }
            if (normalizedLieu.contains(keyword)) {
                score += 1;
            }
        }

        if (event.getType() != null && queryContext.targetedTypes().contains(event.getType())) {
            score += 14;
        }

        String combinedText = normalizedTitle + " " + normalizedDescription + " " + normalizedLieu + " " + normalizedType;

        if (queryContext.matchedThemes().contains("outdoor")
                && containsAny(combinedText, List.of("nature", "plein air", "park", "forest", "beach",
                "camping", "hiking", "adventure", "mountain", "garden"))) {
            score += 10;
        }

        if (queryContext.matchedThemes().contains("music")
                && containsAny(combinedText, List.of("music", "musique", "concert", "festival", "dj", "live"))) {
            score += 12;
        }

        if (queryContext.matchedThemes().contains("sport")
                && containsAny(combinedText, List.of("sport", "football", "gym", "fitness", "match", "race"))) {
            score += 12;
        }

        if (isUpcoming(event)) {
            score += 2;
        }

        if ("OPEN".equalsIgnoreCase(event.getStatus())) {
            score += 2;
        }

        return new ScoredEvent(event, score);
    }

    private boolean isUpcoming(Event event) {
        return event.getStartDate() != null && event.getStartDate().isAfter(LocalDateTime.now().minusDays(1));
    }

    private boolean containsAny(String source, Collection<String> keywords) {
        return keywords.stream().map(this::normalize).anyMatch(source::contains);
    }

    private static Set<String> orderedSet(String... values) {
        return new LinkedHashSet<>(new ArrayList<>(Arrays.asList(values)));
    }

    private AiEventChatResponse emptyResponse(String message) {
        return new AiEventChatResponse(message, List.of());
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ");

        return MULTI_SPACE.matcher(normalized).replaceAll(" ").trim();
    }

    private String buildSuccessAnswer(QueryContext queryContext, int totalMatches) {
        if (queryContext.matchedThemes().contains("outdoor")) {
            return totalMatches == 1
                    ? "Here is 1 matching outdoor event."
                    : "Here are the matching outdoor events.";
        }
        if (queryContext.matchedThemes().contains("sport")) {
            return totalMatches == 1
                    ? "Here is 1 matching sport event."
                    : "Here are the matching sport events.";
        }
        if (queryContext.matchedThemes().contains("music")) {
            return totalMatches == 1
                    ? "Here is 1 matching music event."
                    : "Here are the matching music events.";
        }
        if (queryContext.matchedThemes().contains("conference")) {
            return totalMatches == 1
                    ? "Here is 1 matching conference event."
                    : "Here are the matching conference events.";
        }

        return totalMatches == 1
                ? "Here is 1 matching event."
                : "Here are the matching events.";
    }

    private String buildNoMatchAnswer(String originalMessage) {
        return "I could not find any existing events matching \"" + originalMessage.trim()
                + "\" right now. Try keywords like sport, outdoor, music, festival or conference.";
    }

    private record QueryContext(
            String message,
            Set<String> keywords,
            Set<EventType> targetedTypes,
            Set<String> matchedThemes
    ) {
    }

    private record ThemeDefinition(
            Set<String> triggers,
            Set<String> keywords,
            Set<EventType> eventTypes
    ) {
    }

    private record ScoredEvent(Event event, int score) {
    }
}
