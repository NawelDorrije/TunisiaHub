package org.example.backend_tunisiahub.Controllers.Event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Services.Event.EventShareService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
@Slf4j
public class EventShareController {

    private final EventShareService eventShareService;
    @Value("${app.public-base-url:}")
    private String publicBaseUrl;
    @Value("${app.frontend-list-events-url:}")
    private String frontendListEventsUrl;
    @Value("${app.share.default-image-url:https://via.placeholder.com/1200x630.png?text=TunisiaHub+Event}")
    private String defaultShareImageUrl;

    @GetMapping(value = "/event/{id}/public-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> getPublicShareUrl(@PathVariable Long id, HttpServletRequest request) {
        String baseUrl = resolvePublicBaseUrl(request);
        String url = baseUrl + "/share/event/" + id;
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping(value = "/event/{id}/debug", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> debugShareValues(@PathVariable Long id, HttpServletRequest request) {
        Event event = eventShareService.getEventReadyForShare(id);
        String normalizedBaseUrl = resolvePublicBaseUrl(request);
        String publicShareUrl = normalizedBaseUrl + "/share/event/" + event.getId();
        String marketingDescription = coalesce(event.getMarketingDescription(), event.getDescription());
        String posterImageUrl = resolvePosterUrl(coalesce(event.getPosterImageUrl(), event.getImage()), normalizedBaseUrl);

        return ResponseEntity.ok(Map.of(
                "eventId", String.valueOf(event.getId()),
                "ogTitle", coalesce(event.getTitle(), ""),
                "ogDescription", marketingDescription,
                "ogImage", posterImageUrl,
                "ogUrl", publicShareUrl
        ));
    }

    @GetMapping(value = "/event/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> shareEventPage(@PathVariable Long id, HttpServletRequest request) {
        Event event = eventShareService.getEventReadyForShare(id);

        String normalizedBaseUrl = resolvePublicBaseUrl(request);
        String publicShareUrl = normalizedBaseUrl + "/share/event/" + event.getId();
        String mapsUrl = "https://www.google.com/maps?q=" + event.getLatitude() + "," + event.getLongitude();
        String listEventsUrl = resolveListEventsUrl(normalizedBaseUrl);
        String marketingDescription = coalesce(event.getMarketingDescription(), event.getDescription());
        String posterImageUrl = resolvePosterUrl(coalesce(event.getPosterImageUrl(), event.getImage()), normalizedBaseUrl);

        log.info("Share page OG values | eventId={} | og:title='{}' | og:image='{}' | og:url='{}'",
                event.getId(), event.getTitle(), posterImageUrl, publicShareUrl);

        String html = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>%s</title>
                  <meta property="og:title" content="%s" />
                  <meta property="og:description" content="%s" />
                  <meta property="og:image" content="%s" />
                  <meta property="og:image:secure_url" content="%s" />
                  <meta property="og:image:width" content="1200" />
                  <meta property="og:image:height" content="630" />
                  <meta property="og:url" content="%s" />
                  <meta property="og:type" content="website" />
                  <meta name="twitter:card" content="summary_large_image" />
                  <meta name="twitter:title" content="%s" />
                  <meta name="twitter:description" content="%s" />
                  <meta name="twitter:image" content="%s" />
                </head>
                <body style="font-family:Arial,sans-serif;max-width:760px;margin:30px auto;padding:0 16px;line-height:1.6;">
                  <h1>%s</h1>
                  <p>%s</p>
                  <p>Date: %s -> %s</p>
                  <p>Price: %.2f TND</p>
                  <p>Capacity: %d</p>
                  <p>Location: %s</p>
                  <p><a href="%s" target="_blank" rel="noopener noreferrer">%s</a></p>
                  <p><a href="%s" target="_blank" rel="noopener noreferrer">%s</a></p>
                </body>
                </html>
                """.formatted(
                escapeHtml(event.getTitle()),
                escapeHtml(event.getTitle()),
                escapeHtml(marketingDescription),
                escapeHtml(posterImageUrl),
                escapeHtml(posterImageUrl),
                escapeHtml(publicShareUrl),
                escapeHtml(event.getTitle()),
                escapeHtml(marketingDescription),
                escapeHtml(posterImageUrl),
                escapeHtml(event.getTitle()),
                escapeHtml(marketingDescription),
                escapeHtml(event.getStartDate()),
                escapeHtml(event.getEndDate()),
                event.getPrice(),
                event.getCapacity(),
                escapeHtml(event.getLieu()),
                escapeHtml(mapsUrl),
                escapeHtml(mapsUrl),
                escapeHtml(listEventsUrl),
                escapeHtml(listEventsUrl)
        );

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    private String coalesce(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null ? "" : fallback;
    }

    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String resolvePublicBaseUrl(HttpServletRequest request) {
        String configured = trimTrailingSlash(publicBaseUrl);
        if (!configured.isBlank() && !isLocalUrl(configured)) {
            return configured;
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            String proto = (forwardedProto == null || forwardedProto.isBlank()) ? "https" : forwardedProto;
            return proto + "://" + forwardedHost;
        }

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        return scheme + "://" + host + ((port == 80 || port == 443) ? "" : ":" + port);
    }

    private String resolveListEventsUrl(String normalizedBaseUrl) {
        String configured = trimTrailingSlash(frontendListEventsUrl);
        if (!configured.isBlank() && !isLocalUrl(configured)) {
            return configured;
        }
        return normalizedBaseUrl + "/event/all";
    }

    private String resolvePosterUrl(String posterImageUrl, String normalizedBaseUrl) {
        if (posterImageUrl == null || posterImageUrl.isBlank()) {
            return defaultShareImageUrl;
        }

        if (posterImageUrl.startsWith("http://") || posterImageUrl.startsWith("https://")) {
            if (isLocalUrl(posterImageUrl)) {
                String localPath = extractPathFromUrl(posterImageUrl);
                if (!localPath.isBlank()) {
                    return normalizedBaseUrl + localPath;
                }
                return defaultShareImageUrl;
            }
            return posterImageUrl;
        }

        String path = posterImageUrl.startsWith("/") ? posterImageUrl : "/" + posterImageUrl;
        return normalizedBaseUrl + path;
    }

    private String extractPathFromUrl(String fullUrl) {
        try {
            java.net.URI uri = java.net.URI.create(fullUrl);
            return uri.getPath() == null ? "" : uri.getPath();
        } catch (Exception ex) {
            return "";
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private boolean isLocalUrl(String value) {
        if (value == null) {
            return true;
        }
        String v = value.toLowerCase();
        return v.contains("localhost") || v.contains("127.0.0.1");
    }
}
