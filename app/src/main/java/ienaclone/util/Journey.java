package ienaclone.util;

import java.util.Optional;

import java.util.ArrayList;
import java.time.LocalDateTime;

import javafx.util.Pair;

public class Journey {
    private final String ref;
    private Optional<Line> line;
    private Optional<Stop> destination;
    private Optional<String> mission;
    private Optional<String> platform;
    private TimeStatus timeStatus;
    private PlaceStatus placeStatus;
    private Optional<LocalDateTime> expectedArrivalTime, expectedDepartureTime;
    private Optional<LocalDateTime> aimedArrivalTime, aimedDepartureTime;
    private ArrayList<Pair<Stop, Stop.STATUS>> nextStations;
    private boolean areNextStationsLoaded;

    public Journey(JourneyBuilder builder) {
        assert(builder.ref != null);
        this.ref = builder.ref;
        this.line = Optional.ofNullable(builder.line);
        this.destination = Optional.ofNullable(builder.destination);
        this.mission = Optional.ofNullable(builder.mission);
        this.platform = Optional.ofNullable(builder.platform);
        this.timeStatus = builder.timeStatus;
        this.placeStatus = builder.placeStatus;
        this.expectedArrivalTime = Optional.ofNullable(builder.expectedArrivalTime);
        this.expectedDepartureTime = Optional.ofNullable(builder.expectedDepartureTime);
        this.aimedArrivalTime = Optional.ofNullable(builder.aimedArrivalTime);
        this.aimedDepartureTime = Optional.ofNullable(builder.aimedDepartureTime);
        this.nextStations = new ArrayList<>();
        this.nextStations.addAll(builder.nextStations);
        this.areNextStationsLoaded = false;
    }

    public String getRef() {
        return ref;
    }

    public Optional<Line> getLine() {
        return line;
    }

    public Optional<Stop> getDestination() {
        return destination;
    }

    public Optional<String> getMission() {
        return mission;
    }

    public Optional<String> getPlatform() {
        return platform;
    }

    public TimeStatus getTimeStatus() {
        return timeStatus;
    }

    public PlaceStatus getPlaceStatus() {
        return placeStatus;
    }

    public Optional<LocalDateTime> getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public Optional<LocalDateTime> getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public Optional<LocalDateTime> getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public Optional<LocalDateTime> getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    public boolean areNextStationsLoaded() {
        return areNextStationsLoaded;
    }

    public ArrayList<Pair<Stop, Stop.STATUS>> getNextStations() {
        return nextStations;
    }

    public void setLine(Optional<Line> line) {
        this.line = line;
    }

    public void setDestination(Optional<Stop> destination) {
        this.destination = destination;
    }

    public void setMission(Optional<String> mission) {
        this.mission = mission;
    }

    public void setPlatform(Optional<String> platform) {
        this.platform = platform;
    }

    public void setTimeStatus(TimeStatus timeStatus) {
        this.timeStatus = timeStatus;
    }

    public void setPlaceStatus(PlaceStatus placeStatus) {
        this.placeStatus = placeStatus;
    }

    public void setExpectedArrivalTime(Optional<LocalDateTime> expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public void setExpectedDepartureTime(Optional<LocalDateTime> expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    public void setAimedArrivalTime(Optional<LocalDateTime> aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    public void setAimedDepartureTime(Optional<LocalDateTime> aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    public void setNextStations(ArrayList<Pair<Stop, Stop.STATUS>> nextStations) {
        this.nextStations = nextStations;
    }

    public void setNextStationsLoaded(boolean areNextStationsLoaded) {
        this.areNextStationsLoaded = areNextStationsLoaded;
    }

    public Stop.STATUS getStopStatus(Stop stop) {
        var st = nextStations.stream()
                             .filter(a -> a.getKey().equals(stop))
                             .findFirst();

        if (st.isPresent()) return st.get().getValue();

        return Stop.STATUS.UNKOWN;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Journey)) return false;
        var j = (Journey)obj;
        return j.getRef().equals(this.getRef());
    }

    @Override
    public String toString() {
        String res = mission + " -> ";
        if (expectedArrivalTime.isPresent()) res += expectedArrivalTime;
        else res += expectedDepartureTime;
        return res;
    }
}
