package io.tech1.framework.foundation.domain.mains;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlOptionsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record HtmlOption(String id, String label) {
    }

    @Disabled("development purposes")
    @Test
    void printFormattedDateTimeFormatters() throws JsonProcessingException {
        var pattern1 = "dd.MM.yyyy HH:mm:ss";
        var pattern2 = "dd.MM HH:mm";
        var pattern3 = "dd/MM/yyy HH:mm:ss";
        var patterns = List.of(
                pattern1,
                pattern2,
                pattern3
        );

        var options = patterns.stream()
                .peek(DateTimeFormatter::ofPattern)
                .map(pattern -> new HtmlOption(pattern, pattern))
                .collect(Collectors.toList());

        prettyPrint(options);

        // Assert
        assertThat(options).isNotNull();
    }

    @Disabled("development purposes")
    @Test
    void printFormattedZoneIdsClientConstants() throws JsonProcessingException {
        var ukraineLegacy = ZoneId.of("Europe/Kiev");
        var options = ZoneId.getAvailableZoneIds()
                .stream()
                .map(ZoneId::of)
                .map(ZonedDateTime::now)
                .sorted(Comparator.comparing(ZonedDateTime::getOffset).reversed())
                .map(zdt -> {
                    var zoneId = zdt.getZone().getId();
                    if (zdt.getZone().equals(ukraineLegacy)) {
                        return null;
                    } else {
                        return new HtmlOption(
                                zoneId,
                                "(" + zdt.getOffset() + ")" + " " + zoneId
                        );
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        prettyPrint(options);

        // Assert
        assertThat(options).isNotNull();
    }

    // ================================================================================================================
    // PRIVATE METHODS
    // ================================================================================================================
    private void prettyPrint(List<HtmlOption> options) throws JsonProcessingException {
        var json = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(options);
        json = json.replace("\"id\" ", "id");
        json = json.replace("\"label\" ", "label");
        json = json.replace("\"", "'");
        json = json.replace("[ {", "[{");
        json = json.replace("} ]", "}]");
        System.out.println("===");
        System.out.println(json);
        System.out.println("===");
    }
}
