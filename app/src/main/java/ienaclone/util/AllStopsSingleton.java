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

    public Optional<Stop> getStopByCode(String s) {
        return items.stream()
                    .filter(e -> Parcer.equalsRef(e.getCode(), s))
                    .findFirst();
    }
}
