package ienaclone.util;

public class JourneyBuilder {
    public int order = -1;
    public int numberOfTrains = -1;
    public boolean vehicleAtStop = false;
    public String destinationName, destinationDisplay, destinationRef;
    public String journeyRef;
    public String missionCode;
    public String lineRef;
    public String aimedArrivalTime, aimedDepartureTime;
    public String arrivalPlatform;
    public String arrivalStatus, departureStatus;
    public String expectedArrivalTime, expectedDepartureTime;
    public String stopPointName; 

    public JourneyBuilder() {
        this.order = -1;
        this.numberOfTrains = -1;
        this.vehicleAtStop = false;
        this.destinationName = null;
        this.destinationDisplay = null;
        this.destinationRef = null;
        this.journeyRef = null;
        this.missionCode = null;
        this.lineRef = null;
        this.aimedArrivalTime = null;
        this.aimedDepartureTime = null;
        this.arrivalPlatform = null;
        this.arrivalStatus = null;
        this.departureStatus = null;
        this.expectedArrivalTime = null;
        this.expectedDepartureTime = null;
        this.stopPointName = null;
    }

}
