package ienaclone.util;

import java.util.Optional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoUnit.MINUTES;

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

    public static long getWaitingTime(LocalDateTime toWait) {
        if (toWait == null) return -99999L;
        LocalDateTime now = LocalDateTime.now();
        return MINUTES.between(now, toWait);
    }

    public static String getApiKey() {
        String res = null;

        res = System.getenv("prim_api");

        if (res != null) return res;

        res = Files.getApiKeyFromFile();

        return res;
    }

    public static void writeLog(String text) {
        var time = getCurrentDateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (text.startsWith("\n"))
            System.out.println("\n[" + time + "]\t" + text.substring(1));
        else
            System.out.println("[" + time + "]\t" + text);
    }

    // TODO : à améliorer
    public static String raccourcir(String raw, int limit) {
        String res = raw;
        if (res.length() < limit) return res;
        res = res.replace("Saint", "St");
        if (res.length() < limit) return res;
        res = res.replace("Avenue", "Av.");
        if (res.length() < limit) return res;
        res = res.replace("Porte", "Pte");
        if (res.length() < limit) return res;
        res = res.replace("Notre", "Nte");
        if (res.length() < limit) return res;
        res = res.replace("du Président", "Pdt");
        if (res.length() < limit) return res;
        res = res.replace("sur", "s/");
        if (res.length() < limit) return res;
        res = res.replace("sous", "s/");
        if (res.length() < limit) return res;

        var tab = raw.split(" - ");
        res = tab[0];
        if (res.length() < limit) return res;

        // tab = raw.split(" ");
        // int i = tab.length -1;

        // while(true) {
        //     tab[i] = tab[i].charAt(0) + ".";
        //     res = String.join(" ", tab);
        //     if (res.length() < limit) return res;
        //     i--;
        //     if (i==0) break;
        // }

        return res.substring(0,limit);
    }
}
