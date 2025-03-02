package ienaclone.prim;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.time.LocalDateTime;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import ienaclone.util.Journey;

public class FilterTest {

    ArrayList<Journey> getTestJourneys(String filename) {
        try {
            URL a = RequestsTest.class.getResource(filename);
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
        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        var filtered = Filter.byDirection(journeys, "Meaux");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    // @Test
    // void testByDirectionRef() {
    //     var journeys = getTestJourneys("chelles.json");

    //     assertNotNull(journeys);

    //     var filtered = Filter.byDirectionRef(journeys, "41038");

    //     assertNotNull(filtered);

    //     assertEquals(3, filtered.size());   
    // }

    @Test
    void testByPlatform() {
        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        var filtered = Filter.byPlatform(journeys, "C");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByMission() {
        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        var filtered = Filter.byMission(journeys, "PICI");

        assertNotNull(filtered);

        assertEquals(3, filtered.size());   
    }

    @Test
    void testByLine() {
        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        var filtered = Filter.byLine(journeys, "C01730");

        assertNotNull(filtered);

        assertEquals(7, filtered.size());   
    }

    @Test
    void testRemoveAlreadyPassedTrainsCase1() {
        /* 
         *   cas milieu de journée
         *   = `journeys` contient des passages déjà éffectés et à venir
         */

        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        LocalDateTime now = LocalDateTime.parse("2024-03-09T21:41:18");

        var nextJourneys = Filter.removeAlreadyPassedTrains(journeys, now);

        assertNotNull(nextJourneys);

        for (var j : nextJourneys) {
            System.out.println(j.getExpectedArrivalTime());
        }

        assertEquals(7, nextJourneys.size());
    }

    @Test
    void testRemoveAlreadyPassedTrainsCase2() {
        /* 
         *   cas fin de journée
         *   = `journeys` contient que des passages déjà éffectés
         */

        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        LocalDateTime now = LocalDateTime.parse("2024-03-09T23:00:00");

        var nextJourneys = Filter.removeAlreadyPassedTrains(journeys, now);

        assertNotNull(nextJourneys);

        for (var j : nextJourneys) {
            System.out.println(j.getExpectedArrivalTime());
        }

        assertEquals(0, nextJourneys.size());
    }

    @Test
    void testRemoveAlreadyPassedTrainsCase3() {
        /* 
         *   cas début de journée
         *   = `journeys` contient que des passages à venir
         */

        var journeys = getTestJourneys("chelles.json");

        assertNotNull(journeys);

        LocalDateTime now = LocalDateTime.parse("2024-03-09T19:00:00");

        var nextJourneys = Filter.removeAlreadyPassedTrains(journeys, now);

        assertNotNull(nextJourneys);

        for (var j : nextJourneys) {
            System.out.println(j.getExpectedArrivalTime());
        }

        assertEquals(11, nextJourneys.size());
    }

    @Test
    void testRemoveAlreadyPassedTrainsCase4() {
        /* 
         *   cas 1 seul train déjà passé (et pas le reste)
         */

        var journeys = getTestJourneys("chelles_bis.json");

        assertNotNull(journeys);

        LocalDateTime now = LocalDateTime.parse("2024-08-16T05:10");

        var nextJourneys = Filter.removeAlreadyPassedTrains(journeys, now);

        assertNotNull(nextJourneys);

        for (var j : nextJourneys) {
            System.out.println(j.getExpectedArrivalTime());
        }

        assertEquals(32, nextJourneys.size());
    }


}
