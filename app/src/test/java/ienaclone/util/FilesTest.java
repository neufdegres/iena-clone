package ienaclone.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class FilesTest {
    
    @Test
    void testGetAllStops() {
        var actual = Files.getAllStops();

        assertNotNull(actual);

        String argenteuil = (String)actual.get(4).getCode();

        assertNotNull(argenteuil);

        assertEquals("41166", argenteuil);
    }

    @Test
    void testGetAllLines() {
        var actual = Files.getAllLines();

        assertNotNull(actual);

        String trainE = (String)actual.get(4).getName();

        assertNotNull(trainE);

        assertEquals("E", trainE);
    }

    // @Test
    void testLoadTestNextStopsValues() {
        var data = Files.loadTestNextStopsValues();

        assertNotNull(data);

        var dest = data.get("41038");

        assertNotNull(dest);

        assertEquals(4, dest.size());
    }

    // @Test
    void testGetApiKeyFromFile() {
        String actual = Files.getApiKeyFromFile();

        assertNotNull(actual);
    }
}
