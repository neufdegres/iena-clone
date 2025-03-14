package ienaclone.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class FunctionsTest {
    @Test
    void testGetDateTime() {
        String raw = "2024-08-07T19:43:18.282Z";

        LocalDateTime result = Functions.getDateTime(raw).get();

        assertNotNull(result);

        LocalDateTime expected = LocalDateTime.of(2024, 8, 7, 21, 43, 18);

        assertEquals(expected, result);
    }

    @Test
    void testGetWaitingTime() {
        var from = LocalDateTime.of(2024, 10, 1, 20, 20, 20);
        var toWait = LocalDateTime.of(2024, 10, 1, 20, 20, 40);

        long time = Functions.getWaitingTime(toWait, from);

        assertEquals(20, time);
    }
}
