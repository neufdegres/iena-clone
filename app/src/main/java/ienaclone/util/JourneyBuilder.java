package ienaclone.util;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class JourneyBuilder {
    public String ref;
    public Line line;
    public Stop destination;
    public String mission;
    public String platform;
    public TimeStatus timeStatus;
    public PlaceStatus placeStatus;
    public LocalDateTime expectedArrivalTime, expectedDepartureTime;
    public LocalDateTime aimedArrivalTime, aimedDepartureTime;
    public ArrayList<String> nextStations;

    public JourneyBuilder() {
        this.line = null;
        this.destination = null;
        this.mission = null;
        this.platform = null;
        this.timeStatus = TimeStatus.UNKNOWN;
        this.placeStatus = PlaceStatus.UNKNOWN;
        this.expectedArrivalTime = null;
        this.expectedDepartureTime = null;
        this.aimedArrivalTime = null;
        this.aimedDepartureTime = null;
        this.nextStations = new ArrayList<>();
    }
}
