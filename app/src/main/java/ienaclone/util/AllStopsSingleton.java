package ienaclone.util;

import java.util.ArrayList;
import java.util.Optional;

import ienaclone.prim.Parcer;

public class AllStopsSingleton {
    private static volatile AllStopsSingleton instance;
    private ArrayList<Stop> items;

    private AllStopsSingleton() {
        items = Files.getAllStops();
    }

    public static AllStopsSingleton getInstance() {
        AllStopsSingleton res = instance;
        if (instance != null) return res;

        synchronized(AllStopsSingleton.class) {
            if (instance == null) {
                instance = new AllStopsSingleton();
            }
            return instance;
        }
    }

    public ArrayList<Stop> getItems() {
        return items;
    }

    public Optional<Stop> getStopByPointId(String s) {
        return items.stream()
                    .filter(e -> Parcer.equalsRef(e.getPointId(), s))
                    .findFirst();
    }

    public Optional<Stop> getStopByAreaId(String s) {
        return items.stream()
                    .filter(e -> Parcer.equalsRef(e.getAreaId(), s))
                    .findFirst();
    }

    public Optional<Stop> getStopByTransporterId(String s) {
        return items.stream()
                    .filter(e -> e.getTransporterIds()
                                  .stream()
                                  .filter(e1 -> Parcer.equalsRef(e1, s))
                                  .findFirst()
                                  .isPresent())
                    .findFirst();
    }
}
