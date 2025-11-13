package ienaclone.util;

import java.util.ArrayList;

import ienaclone.prim.Parcer;

public class Stop {
    private final String pointId, areaId;
    private final ArrayList<String> transporterIds;
    private final String name;
    private final boolean isParis, isRATP;
    private final ArrayList<String> lines;

    public enum STATUS {INCLUDED, SKIPPED, START, TERMINUS, UNKNOWN}

    public Stop(String pointId,
                String areaId,
                ArrayList<String> transporterIds, 
                String name,
                boolean isParis,
                boolean isRATP,
                ArrayList<String> lines) {
        this.pointId = pointId;
        this.areaId = areaId;
        this.transporterIds = transporterIds;
        this.name = name;
        this.isParis = isParis;
        this.isRATP = isRATP;
        this.lines = lines;
    }

    public Stop(String pointId, String name) {
        this(pointId, "", new ArrayList<>(), name, false, false, new ArrayList<>());
    }

    public Stop() {
        this("N/A", "N/A");
    }

    public String getPointId() {
        return pointId;
    }

    public String getAreaId() {
        return areaId;
    }

    public ArrayList<String> getTransporterIds() {
        return transporterIds;
    }

    public String getName() {
        return name;
    }

    public boolean isParis() {
        return isParis;
    }

    public boolean isRATP() {
        return isRATP;
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
        return STATUS.UNKNOWN;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Stop)) return false;

        Stop curr = (Stop)obj;

        return Parcer.equalsRef(curr.pointId, this.pointId)
            && Parcer.equalsRef(curr.areaId, this.areaId);
    }
    
    @Override
    public String toString() {
        return "[" + pointId + "] " + name + " -> " + lines.toString();
    }
}
