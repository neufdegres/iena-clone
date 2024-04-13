package ienaclone.util;

import java.util.ArrayList;
import java.util.Optional;

import ienaclone.prim.Parcer;

public class AllLinesSingleton {
    private static volatile AllLinesSingleton instance;
    private ArrayList<Line> items;

    private AllLinesSingleton() {
        items = Files.getAllLines();
    }

    public static AllLinesSingleton getInstance() {
        AllLinesSingleton res = instance;
        if (instance != null) return res;

        synchronized(AllLinesSingleton.class) {
            if (instance == null) {
                instance = new AllLinesSingleton();
            }
            return instance;
        }
    }

    public Optional<Line> getLineByName(String s) {
        return items.stream()
                    .filter(e -> e.getName().equals(s))
                    .findFirst();
    }

    public Optional<Line> getLineByCode(String s) {
        return items.stream()
                    .filter(e -> e.getCode().equals(Parcer.getRefCode(s)))
                    .findFirst();
    }
}
