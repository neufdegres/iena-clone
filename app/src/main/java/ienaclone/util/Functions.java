package ienaclone.util;

import java.util.Optional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Functions {

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    public static Optional<LocalDateTime> getDateTime(String raw) {
        if (raw.length() < 19) return Optional.empty();
        raw = raw.substring(0, 19);
        raw = raw.replace('T', ' ');
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt = LocalDateTime.parse(raw, formatter);

        ZoneId paris = ZoneId.of("Europe/Paris");
        ZoneId gmt = ZoneId.of("GMT");

        ZonedDateTime timeInGMT = ZonedDateTime.of(dt, gmt);
        ZonedDateTime timeInParis = timeInGMT.withZoneSameInstant(paris);

        return Optional.ofNullable(timeInParis.toLocalDateTime());
    }
}
