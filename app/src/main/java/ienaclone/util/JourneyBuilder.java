package ienaclone.util;

public class JourneyBuilder {
    public String destinationName, destinationRef;
    public String journeyRef;
    public String missionCode;
    public String lineRef;
    public String arrivalPlatform;
    public String expectedArrivalTime, expectedDepartureTime;

    public JourneyBuilder() {
        this.destinationName = null;
        this.destinationRef = null;
        this.journeyRef = null;
        this.missionCode = null;
        this.lineRef = null;
        this.arrivalPlatform = null;
        this.expectedArrivalTime = null;
        this.expectedDepartureTime = null;
    }

}
