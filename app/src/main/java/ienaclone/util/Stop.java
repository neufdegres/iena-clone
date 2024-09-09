package ienaclone.util;

import java.util.ArrayList;

import ienaclone.prim.Parcer;

public class Stop {
    private final String code;
    private final String name;
    private final boolean isParis;
    private final ArrayList<String> lines;

    public enum STATUS {INCLUDED, SKIPPED, START, TERMINUS, UNKOWN}

    public Stop(String code, String name, boolean isParis, ArrayList<String> lines) {
        this.code = code;
        this.name = name;
        this.isParis = isParis;
        this.lines = lines;
    }

    public Stop(String code, String name) {
        this(code, name, false, new ArrayList<>());
    }

    public Stop() {
        this("N/A", "N/A");
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isParis() {
        return isParis;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    public static STATUS getStatus(String txt) {
        switch (txt) {
            case "start":
                return STATUS.START;
            case "included":
            return STATUS.INCLUDED;
            case "skipped":
            return STATUS.SKIPPED;
            case "terminus":
            return STATUS.TERMINUS;
        }
        return STATUS.UNKOWN;
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
