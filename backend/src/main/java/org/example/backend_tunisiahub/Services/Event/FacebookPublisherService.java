package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FacebookPublisherService {

    private final EventShareService eventShareService;

    @Value("${facebook.page.id:}")
    private String pageId;

    @Value("${facebook.page.token:}")
    private String pageToken;

    public String publishEventToPage(Long eventId) {
        if (pageId == null || pageId.isBlank() || pageToken == null || pageToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Facebook is not configured. Add facebook.page.id and facebook.page.token."
            );
        }

        Event event = eventShareService.getEventReadyForShare(eventId);
        String posterUrl = resolvePosterUrl(event);
        String caption = buildCaption(event);

        String graphUrl = "https://graph.facebook.com/v19.0/" + pageId + "/photos";
        String formBody = "published=true"
                + "&url=" + URLEncoder.encode(posterUrl, StandardCharsets.UTF_8)
                + "&caption=" + URLEncoder.encode(caption, StandardCharsets.UTF_8)
                + "&access_token=" + URLEncoder.encode(pageToken, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(graphUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Facebook publish failed: " + response.body()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Facebook publish request interrupted.", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Facebook publish request failed.", e);
        }
    }

    private String buildCaption(Event event) {
        String startDate = event.getStartDate() == null
                ? "N/A"
                : event.getStartDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm"));
        String endDate = event.getEndDate() == null
                ? "N/A"
                : event.getEndDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm"));
        String description = (event.getMarketingDescription() != null && !event.getMarketingDescription().isBlank())
                ? event.getMarketingDescription()
                : (event.getDescription() == null ? "" : event.getDescription());
        String location = event.getLieu() == null ? "N/A" : event.getLieu();

        return "Event: " + safe(event.getTitle()) + "\n\n"
                + description + "\n\n"
                + "Date: " + startDate + " -> " + endDate + "\n"
                + "Location: " + location;
    }

    private String resolvePosterUrl(Event event) {
        String poster = event.getPosterImageUrl();
        if (poster == null || poster.isBlank()) {
            poster = event.getImage();
        }
        if (poster == null || poster.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No poster/image available for this event."
            );
        }
        return poster;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
