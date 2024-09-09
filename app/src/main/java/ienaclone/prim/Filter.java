package ienaclone.prim;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import java.time.LocalDateTime;

import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.Line;
import ienaclone.util.Stop;

public class Filter {

    public static List<Journey> byDirection(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getDestination().orElse(new Stop())
                                .getName().equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byDirectionRef(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> Parcer.equalsRef(
                        j.getDestination().orElse(new Stop()).getCode(), filter))
                  .collect(Collectors.toList());
    }
    
    public static List<Journey> byPlatform(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getPlatform().orElse("N/A").equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byMission(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> j.getMission().orElse("N/A").equals(filter))
                  .collect(Collectors.toList());
    }

    public static List<Journey> byLine(ArrayList<Journey> all, String filter) {
        return all.stream()
                  .filter(j -> Parcer.equalsRef(
                        j.getLine().orElse(new Line("N/A")).getCode(), filter))
                  .collect(Collectors.toList());
    }

    public static ArrayList<Journey> removeAlreadyPassedTrains(ArrayList<Journey> all, LocalDateTime now) {
        var res = new ArrayList<Journey>();
        if (now == null) now = Functions.getCurrentDateTime();
        Optional<LocalDateTime> departure = null, arrival = null;
        int len = all.size();
        int i = len-1;
        boolean toRemove = false;
        for (; i>=0; i-- ) { 
            toRemove = false;
            var tmp = all.get(i);

            departure = tmp.getExpectedDepartureTime();
            arrival = tmp.getExpectedArrivalTime();

            if (departure.isEmpty() && arrival.isEmpty()) {
                if (i>0) continue;
            } else if (departure.isEmpty()) {
                if (now.isBefore(arrival.get())) {
                    if (i>0) continue;
                } else {
                    toRemove = true;
                }
            } else {
                if (now.isBefore(departure.get())) {
                    if (i>0) continue;
                } else {
                    toRemove = true;
                }
            }

            if (i > 0 || toRemove)
                res.addAll(all.subList(i+1, len));
            else 
                res.addAll(all);
            
                break;
        }
        return res;
    }

}
