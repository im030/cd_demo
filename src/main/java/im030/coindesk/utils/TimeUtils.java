package im030.coindesk.utils;

import com.fasterxml.jackson.databind.JsonNode;
import im030.coindesk.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtils {
    private static final Logger log = LoggerFactory.getLogger(TimeUtils.class);
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public static OffsetDateTime updatedAtFrom(JsonNode time) {
        if (time == null || time.get("updatedISO") == null) return null;
        String iso = time.get("updatedISO").asText();
        if (!StringUtils.hasText(iso)) return null;
        try {
            return OffsetDateTime.parse(iso);
        } catch (DateTimeParseException e) {
            log .warn("cannot parse updatedISO: {}", iso);
            return null;
        }
    }

    public static String formattedTimeFromJson(JsonNode time) {
        return updatedAtFrom(time)
                .atZoneSameInstant(ZoneId.of("Asia/Taipei"))
                .format(TimeUtils.timeFormatter);
    }
}
