package ienaclone.util;

import ienaclone.prim.Parcer;

public class Line {
    private final String name;
    private final String code;
    private final String color;

    public Line(String name, String code, String color) {
        this.name = name;
        this.code = code;
        this.color = color;
    }

    public Line(String code) {
        // TODO : cherhcer le nom de la ligne correspondante 
        this(null, code, null);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Line)) return false;

        Line curr = (Line)obj;

        return Parcer.equalsRef(curr.code, this.code);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(code).append("] ");
        sb.append(name).append("\n");

        return sb.toString();
    }
}
