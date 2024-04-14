package ienaclone.prim;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ienaclone.util.Journey;

public class Filter {

    public static List<Journey> byDirection(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getDestinationName().equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byDirectionRef(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> Parcer.equalsRef(j.getDestinationRef(), filter))
                  .collect(Collectors.toList());
    }
    
    public static List<Journey> byPlatform(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getArivalPlatform().equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byMission(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getMissionCode().equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byLine(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> Parcer.equalsRef(j.getLineRef(), filter))
                  .collect(Collectors.toList());
    }
}
