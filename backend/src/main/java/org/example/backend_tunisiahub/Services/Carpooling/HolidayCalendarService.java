package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.HolidayCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class HolidayCalendarService implements IHolidayCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(HolidayCalendarService.class);
    private static final String SOURCE_URL_TEMPLATE = "https://www.timeanddate.com/holidays/tunisia/%s?hol=9";
    private static final String FALLBACK_SOURCE_URL_TEMPLATE = "https://www.officeholidays.com/countries/tunisia/%s";
    private static final Pattern HOLIDAY_LINE_PATTERN = Pattern.compile(
            "^(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+[A-Za-z]+\\s+(.+?)\\s+(Public Holiday|Observance|Season)$"
    );
    private static final Pattern OFFICE_HOLIDAY_LINE_PATTERN = Pattern.compile(
            "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+" +
                    "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+" +
                    "(\\d{1,2})\\s+(.+?)\\s+National Holiday"
    );
    private static final Set<String> CRITICAL_HOLIDAYS = Set.of(
            "ramadan start",
            "eid al-fitr",
            "eid al-fitr holiday",
            "eid al-adha",
            "eid al-adha holiday",
            "aid el-fitr",
            "aid el kebir",
            "the prophet's birthday",
            "the prophets birthday",
            "prophet mohammed's birthday",
            "prophet mohammeds birthday"
    );

    @Override
    public List<HolidayCalendar> retrieveHolidaysByYear(Integer year) {
        int safeYear = year != null ? year : LocalDate.now().getYear();
        String sourceUrl = SOURCE_URL_TEMPLATE.formatted(safeYear);
        String html = fetchPage(sourceUrl);
        List<HolidayCalendar> holidays = parseHolidays(html, safeYear, sourceUrl).stream()
                .sorted(Comparator.comparing(HolidayCalendar::getHolidayDate))
                .toList();
        if (holidays.isEmpty()) {
            String fallbackSourceUrl = FALLBACK_SOURCE_URL_TEMPLATE.formatted(safeYear);
            logger.warn("Primary holiday source returned no critical holidays year={} source={}. Trying fallback source={}",
                    safeYear,
                    sourceUrl,
                    fallbackSourceUrl);
            holidays = parseOfficeHolidays(fetchPage(fallbackSourceUrl), safeYear, fallbackSourceUrl).stream()
                    .sorted(Comparator.comparing(HolidayCalendar::getHolidayDate))
                    .toList();
        }
        logger.info("Holiday scrape completed year={} source={} criticalHolidays={}",
                safeYear,
                holidays.isEmpty() ? sourceUrl : holidays.get(0).getSourceUrl(),
                holidays.size());
        for (HolidayCalendar holiday : holidays) {
            logger.info("Holiday critical match name={} date={} type={} tentative={}",
                    holiday.getHolidayName(),
                    holiday.getHolidayDate(),
                    holiday.getHolidayType(),
                    holiday.getTentative());
        }
        return holidays;
    }

    private String fetchPage(String sourceUrl) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sourceUrl))
                .header("User-Agent", "Mozilla/5.0 TunisiaHub Holiday Sync")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            logger.info("Holiday page retrieved source={} status={} bodyLength={}",
                    sourceUrl,
                    response.statusCode(),
                    response.body() != null ? response.body().length() : 0);
            return response.body();
        } catch (IOException | InterruptedException exception) {
            throw new IllegalStateException("Unable to retrieve Tunisia holiday page", exception);
        }
    }

    private List<HolidayCalendar> parseHolidays(String html, int year, String sourceUrl) {
        String text = convertHtmlToText(html);
        String[] lines = text.split("\\R");
        Map<String, HolidayCalendar> holidaysByKey = new LinkedHashMap<>();
        int matchedRows = 0;
        int ignoredRows = 0;

        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            Matcher matcher = HOLIDAY_LINE_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            matchedRows++;

            String holidayName = matcher.group(3).trim();
            if (holidayName.equalsIgnoreCase("March Equinox")
                    || holidayName.equalsIgnoreCase("June Solstice")
                    || holidayName.equalsIgnoreCase("September Equinox")
                    || holidayName.equalsIgnoreCase("December Solstice")) {
                continue;
            }

            String cleanedHolidayName = cleanHolidayName(holidayName);
            if (!isCriticalHoliday(cleanedHolidayName)) {
                ignoredRows++;
                continue;
            }

            int day = Integer.parseInt(matcher.group(1));
            Month month = resolveMonth(matcher.group(2));
            LocalDate holidayDate = LocalDate.of(year, month, day);
            String holidayType = matcher.group(4).trim();
            boolean tentative = holidayName.toLowerCase(Locale.ROOT).contains("tentative date");

            HolidayCalendar holidayCalendar = new HolidayCalendar();
            holidayCalendar.setYearValue(year);
            holidayCalendar.setHolidayName(cleanedHolidayName);
            holidayCalendar.setHolidayDate(holidayDate);
            holidayCalendar.setHolidayType(holidayType);
            holidayCalendar.setHolidayCategory(resolveHolidayCategory(holidayName));
            holidayCalendar.setCriticalForCarpooling(true);
            holidayCalendar.setTentative(tentative);
            holidayCalendar.setSourceUrl(sourceUrl);
            holidayCalendar.setSyncedAt(LocalDateTime.now());

            String key = holidayCalendar.getHolidayDate() + "|" + holidayCalendar.getHolidayName();
            holidaysByKey.putIfAbsent(key, holidayCalendar);
        }

        logger.info("Holiday parse result year={} matchedRows={} ignoredRows={} criticalRows={}",
                year,
                matchedRows,
                ignoredRows,
                holidaysByKey.size());
        return new ArrayList<>(holidaysByKey.values());
    }

    private List<HolidayCalendar> parseOfficeHolidays(String html, int year, String sourceUrl) {
        String text = normalizeLine(convertHtmlToText(html));
        Map<String, HolidayCalendar> holidaysByKey = new LinkedHashMap<>();
        int matchedRows = 0;
        int ignoredRows = 0;

        Matcher matcher = OFFICE_HOLIDAY_LINE_PATTERN.matcher(text);
        while (matcher.find()) {
            matchedRows++;

            String holidayName = matcher.group(4).trim();
            String cleanedHolidayName = cleanHolidayName(resolveFallbackHolidayName(holidayName));
            if (!isCriticalHoliday(cleanedHolidayName)) {
                ignoredRows++;
                continue;
            }

            int day = Integer.parseInt(matcher.group(3));
            Month month = resolveMonth(matcher.group(2));
            LocalDate holidayDate = LocalDate.of(year, month, day);

            HolidayCalendar holidayCalendar = new HolidayCalendar();
            holidayCalendar.setYearValue(year);
            holidayCalendar.setHolidayName(cleanedHolidayName);
            holidayCalendar.setHolidayDate(holidayDate);
            holidayCalendar.setHolidayType("National Holiday");
            holidayCalendar.setHolidayCategory(resolveHolidayCategory(cleanedHolidayName));
            holidayCalendar.setCriticalForCarpooling(true);
            holidayCalendar.setTentative(false);
            holidayCalendar.setSourceUrl(sourceUrl);
            holidayCalendar.setSyncedAt(LocalDateTime.now());

            String key = holidayCalendar.getHolidayDate() + "|" + holidayCalendar.getHolidayName();
            holidaysByKey.putIfAbsent(key, holidayCalendar);
        }

        logger.info("Fallback holiday parse result year={} matchedRows={} ignoredRows={} criticalRows={}",
                year,
                matchedRows,
                ignoredRows,
                holidaysByKey.size());
        return new ArrayList<>(holidaysByKey.values());
    }

    private String convertHtmlToText(String html) {
        String cleaned = html;
        cleaned = cleaned.replaceAll("(?is)<script.*?</script>", " ");
        cleaned = cleaned.replaceAll("(?is)<style.*?</style>", " ");
        cleaned = cleaned.replaceAll("(?i)</tr>", "\n");
        cleaned = cleaned.replaceAll("(?i)<br\\s*/?>", "\n");
        cleaned = cleaned.replaceAll("(?i)</p>", "\n");
        cleaned = cleaned.replaceAll("(?i)</div>", "\n");
        cleaned = cleaned.replaceAll("(?i)</li>", "\n");
        cleaned = cleaned.replaceAll("(?i)<[^>]+>", " ");
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("&#39;", "'");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("’", "'");
        cleaned = cleaned.replace("–", "-");
        return cleaned;
    }

    private String normalizeLine(String line) {
        return line.replaceAll("\\s+", " ").trim();
    }

    private Month resolveMonth(String monthValue) {
        return switch (monthValue) {
            case "Jan" -> Month.JANUARY;
            case "Feb" -> Month.FEBRUARY;
            case "Mar" -> Month.MARCH;
            case "Apr" -> Month.APRIL;
            case "May" -> Month.MAY;
            case "Jun" -> Month.JUNE;
            case "Jul" -> Month.JULY;
            case "Aug" -> Month.AUGUST;
            case "Sep" -> Month.SEPTEMBER;
            case "Oct" -> Month.OCTOBER;
            case "Nov" -> Month.NOVEMBER;
            default -> Month.DECEMBER;
        };
    }

    private String cleanHolidayName(String holidayName) {
        return holidayName.replace("(Tentative Date)", "").trim();
    }

    private boolean isCriticalHoliday(String holidayName) {
        return CRITICAL_HOLIDAYS.contains(normalizeHolidayKey(holidayName));
    }

    private String normalizeHolidayKey(String holidayName) {
        String normalized = Normalizer.normalize(holidayName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized
                .replace("’", "'")
                .replace("'", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String resolveFallbackHolidayName(String holidayName) {
        String normalized = normalizeHolidayKey(holidayName);
        if (normalized.contains("aid el-fitr")) {
            return "Eid al-Fitr Holiday";
        }
        if (normalized.contains("aid el kebir")) {
            return "Eid al-Adha";
        }
        if (normalized.contains("prophet mohammed")) {
            return "The Prophet's Birthday";
        }
        return holidayName;
    }

    private String resolveHolidayCategory(String holidayName) {
        String normalized = holidayName.toLowerCase(Locale.ROOT);
        if (normalized.contains("ramadan")
                || normalized.contains("eid")
                || normalized.contains("muharram")
                || normalized.contains("prophet")) {
            return "religious";
        }

        if (normalized.contains("martyrs")
                || normalized.contains("independence")
                || normalized.contains("labour")
                || normalized.contains("republic")
                || normalized.contains("women")
                || normalized.contains("evacuation")
                || normalized.contains("revolution")
                || normalized.contains("new year")) {
            return "national";
        }

        return "other";
    }
}
