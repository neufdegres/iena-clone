package ienaclone.prim;

import java.util.ArrayList;
import ienaclone.util.AllLinesSingleton;
import ienaclone.util.Journey;
import ienaclone.util.Line;
import ienaclone.util.Stop;

public class Parcer {
    
    public static ArrayList<Stop> parseDirectionsFromData(ArrayList<Journey> data) {
        ArrayList<Stop> res = new ArrayList<>();
        for (var j : data) {
            Stop tmp = new Stop(j.getDestinationRef(), j.getDestinationName());
            if (!res.contains(tmp)) res.add(tmp);
        }   

        return res;
    }

    public static ArrayList<String> parsePlatformsFromData(ArrayList<Journey> data) {
        ArrayList<String> res = new ArrayList<>();

        for (var j : data) {
            String pltf = j.getArivalPlatform();
            if (!res.contains(pltf)) res.add(pltf);
        }   

        return res;
    }

    public static ArrayList<String> parseMissionsFromData(ArrayList<Journey> data) {
        ArrayList<String> res = new ArrayList<>();

        for (var j : data) {
            String miss = j.getMissionCode();
            if (!res.contains(miss)) res.add(miss);
        }   

        return res;
    }

    public static ArrayList<Line> parseLinesFromData(ArrayList<Journey> data) {
        ArrayList<Line> res = new ArrayList<>();

        for (var j : data) {
            var allLines = AllLinesSingleton.getInstance();
            Line tmp = allLines.getLineByCode(j.getLineRef()).get();
            if(!res.contains(tmp)) res.add(tmp);
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
}
