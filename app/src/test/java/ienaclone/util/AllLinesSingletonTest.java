package ienaclone.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AllLinesSingletonTest {
    
    @Test
    void getLineByNameTest() {
        var allLines = AllLinesSingleton.getInstance();

        assertNotNull(allLines);

        var actual = allLines.getLineByName("E").get();

        assertNotNull(actual);

        assertEquals(new Line("E", "0").getName(), actual.getName());
    }
}
