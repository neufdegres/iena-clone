package ienaclone.prim;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import ienaclone.util.Journey;

public class FilterTest {

    ArrayList<Journey> getTestJourneys() {
        try {
            URL a = RequestsTest.class.getResource("chelles.json");
            File file = new File(a.toURI());

            if (file.exists()){
                InputStream is;
                is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                
                JSONObject json = new JSONObject(jsonTxt);

                return Requests.parseNextJourneys(json);
            }
        } catch (IOException | URISyntaxException e) {
            return null;
        }

        return null;
    }

    @Test
    void testByDirection() {
        var journeys = getTestJourneys();

        assertNotNull(journeys);

        var filtered = Filter.byDirection(journeys, "Meaux");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByDirectionRef() {
        var journeys = getTestJourneys();

        assertNotNull(journeys);

        var filtered = Filter.byDirectionRef(journeys, "41038");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByPlatform() {
        var journeys = getTestJourneys();

        assertNotNull(journeys);

        var filtered = Filter.byPlatform(journeys, "C");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByMission() {
        var journeys = getTestJourneys();

        assertNotNull(journeys);

        var filtered = Filter.byMission(journeys, "PICI");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByLine() {
        var journeys = getTestJourneys();

        assertNotNull(journeys);

        var filtered = Filter.byLine(journeys, "C01730");

        assertNotNull(filtered);

        assertEquals(7, filtered.size());   
    }
}
