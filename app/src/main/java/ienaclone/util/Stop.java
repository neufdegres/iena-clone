package ienaclone.util;

import java.util.ArrayList;

import ienaclone.prim.Parcer;

public class Stop {
    private final String code;
    private final String name;
    private final ArrayList<String> lines;

    public Stop(String code, String name, ArrayList<String> lines) {
        this.code = code;
        this.name = name;
        this.lines = lines;
    }

    public Stop(String code, String name) {
        this(code, name, new ArrayList<>());
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Stop)) return false;

        Stop curr = (Stop)obj;

        return Parcer.equalsRef(curr.code, this.code);
    }
    
    @Override
    public String toString() {
        return "[" + code + "] " + name + " -> " + lines.toString();
    }
}
