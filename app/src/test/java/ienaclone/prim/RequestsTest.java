package ienaclone.prim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class RequestsTest {
    @Test
    void testGetNextJourneys() {
        try {
            URL a = RequestsTest.class.getResource("chelles.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);

                var journeys = Requests.parseNextJourneys(json);

                assertNotNull(journeys);

                var c0 = journeys.get(0);

                assertNotNull(c0);

                // System.out.println(c0.toString());

                LocalDateTime expectedArrivalTime = LocalDateTime.parse("2024-03-09T21:13:27");

                var actual = c0.getExpectedArrivalTime().orElse(null);

                assertNotNull(actual);

                assertEquals(expectedArrivalTime, actual);
            }
        } catch (IOException | URISyntaxException e) {
            assertFalse(true);
        }
    }

    @Test
    void testParseJourneyStopList() {
        try {
            URL a = RequestsTest.class.getResource("E.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);

                var stops = Requests.parseJourneyStopList(json);

                assertNotNull(stops);

                var actual = stops.get(1);

                assertNotNull(actual);

                String expected = "41126"; // Magenta

                assertEquals(expected, actual);
            }
        } catch (IOException | URISyntaxException e) {
            assertFalse(true);
        }
    }
}
