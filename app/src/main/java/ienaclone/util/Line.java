package ienaclone.util;

import ienaclone.prim.Parcer;

public class Line {
    private final String name;
    private final String code;

    public Line(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Line(String code) {
        // TODO : cherhcer le nom de la ligne correspondante 
        this(null, code);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Line)) return false;

        Line curr = (Line)obj;

        return Parcer.getRefCode(curr.code).equals(Parcer.getRefCode(this.code));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(code).append("] ");
        sb.append(name).append("\n");

        return sb.toString();
    }
}
