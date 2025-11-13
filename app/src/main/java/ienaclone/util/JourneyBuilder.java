package ienaclone.util;

import java.util.ArrayList;
import java.time.LocalDateTime;

import javafx.util.Pair;

public class JourneyBuilder {
    public String ref;
    public Line line;
    public Stop destination;
    public String mission;
    public String missionRATP;
    public String platform;
    public RefStatus refStatus;
    public TimeStatus timeStatus;
    public PlaceStatus placeStatus;
    public TrainLength trainLength;
    public LocalDateTime expectedArrivalTime, expectedDepartureTime;
    public LocalDateTime aimedArrivalTime, aimedDepartureTime;
    public ArrayList<Stop> nextStationsBis;
    public ArrayList<Pair<Stop, Stop.STATUS>> nextStations;

    public JourneyBuilder() {
        this.line = null;
        this.destination = null;
        this.mission = null;
        this.missionRATP = null;
        this.platform = null;
        this.refStatus = RefStatus.SNCF_LOADED;
        this.timeStatus = TimeStatus.UNKNOWN;
        this.placeStatus = PlaceStatus.UNKNOWN;
        this.trainLength = TrainLength.UNKNOWN;
        this.expectedArrivalTime = null;
        this.expectedDepartureTime = null;
        this.aimedArrivalTime = null;
        this.aimedDepartureTime = null;
        this.nextStations = new ArrayList<>();
    }
}
