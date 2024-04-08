package ienaclone.util;

import java.util.ArrayList;

public class Stop {
    private final String code;
    private final String name;
    private final ArrayList<String> lines;

    public Stop(String code, String name, ArrayList<String> lines) {
        this.code = code;
        this.name = name;
        this.lines = lines;
    }

    // public Stop(HashMap<String, Object> data) {
    //     this.code = (String)data.get("code");
    //     this.name = (String)data.get("name");
    //     this.lines = (ArrayList<String>)data.get("lines");
    // }

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
    public String toString() {
        return "[" + code + "] " + name + " -> " + lines.toString();
    }
}
