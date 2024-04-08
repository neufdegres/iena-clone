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
}
