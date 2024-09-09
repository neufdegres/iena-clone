package ienaclone.prim;

import java.util.ArrayList;
import ienaclone.util.Journey;
import ienaclone.util.Line;
import ienaclone.util.Stop;

public class Parcer {

    public static ArrayList<Stop> parseDirectionsFromData(ArrayList<Journey> data) {
        ArrayList<Stop> res = new ArrayList<>();
        for (var j : data) {
            Stop tmp = j.getDestination().orElse(new Stop());
            if (!res.contains(tmp)) res.add(tmp);
        }   

        return res;
    }

    public static ArrayList<String> parsePlatformsFromData(ArrayList<Journey> data) {
        ArrayList<String> res = new ArrayList<>();

        for (var j : data) {
            String pltf = j.getPlatform().orElse("N/A");
            if (!res.contains(pltf)) res.add(pltf);
        }   

        return res;
    }

    public static ArrayList<String> parseMissionsFromData(ArrayList<Journey> data) {
        ArrayList<String> res = new ArrayList<>();

        for (var j : data) {
            String miss = j.getMission().orElse("N/A");
            if (!res.contains(miss)) res.add(miss);
        }   

        return res;
    }

    public static ArrayList<Line> parseLinesFromData(ArrayList<Journey> data) {
        ArrayList<Line> res = new ArrayList<>();

        for (var j : data) {
            var tmp = j.getLine();
            if (tmp.isPresent()) {
                if(!res.contains(tmp.get()))
                    res.add(tmp.get());
            }
        }   

        return res;
    }

    public static String getRefCode(String ref) {
        // STIF:StopPoint:Q:41010: -> stop ref
        // STIF:Line::C01730: -> line ref
        var tab = ref.split(":");
        if (tab.length != 4) return ref;
        return tab[3];
    }

    public static boolean equalsRef(String c1, String c2) {
        return getRefCode(c1).equals(getRefCode(c2));
    }
}
