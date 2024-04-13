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

                String expectedArrivalTime = "2024-03-09T20:13:27.000Z";

                assertEquals(expectedArrivalTime, c0.getExpectedArrivalTime());
            }
        } catch (IOException | URISyntaxException e) {
            assertFalse(true);
        }
    }
}
