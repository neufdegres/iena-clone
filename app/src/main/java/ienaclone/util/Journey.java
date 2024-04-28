package ienaclone.util;

public class Journey {
    private String destinationName, destinationRef; 
    private String journeyRef;
    private String missionCode;
    private String lineRef;
    private String arrivalPlatform;
    private String expectedArrivalTime, expectedDepartureTime;

    public Journey(JourneyBuilder builder) {
        this.destinationName = builder.destinationName != null ? builder.destinationName : "N/A";
        this.destinationRef = builder.destinationRef != null ? builder.destinationRef : "N/A";
        this.journeyRef = builder.journeyRef != null ? builder.journeyRef : "N/A";
        this.missionCode = builder.missionCode != null ? builder.missionCode : "N/A";
        this.lineRef = builder.lineRef != null ? builder.lineRef : "N/A";
        this.arrivalPlatform = builder.arrivalPlatform != null ? builder.arrivalPlatform : "N/A";
        this.expectedArrivalTime = builder.expectedArrivalTime != null ? builder.expectedArrivalTime : "N/A";
        this.expectedDepartureTime = builder.expectedDepartureTime != null ? builder.expectedDepartureTime : "N/A";
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationRef() {
        return destinationRef;
    }

    public void setDestinationRef(String destinationRef) {
        this.destinationRef = destinationRef;
    }

    public String getJourneyRef() {
        return journeyRef;
    }

    public void setJourneyRef(String journeyRef) {
        this.journeyRef = journeyRef;
    }

    public String getMissionCode() {
        return missionCode;
    }

    public void setMissionCode(String missionCode) {
        this.missionCode = missionCode;
    }

    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public String getArivalPlatform() {
        return arrivalPlatform;
    }

    public void setArrivalPlatform(String arrivalPlatform) {
        this.arrivalPlatform = arrivalPlatform;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public String getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime(String expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("destinationName: ").append(destinationName).append("\n");
        sb.append("destinationRef: ").append(destinationRef).append("\n");
        sb.append("journeyRef: ").append(journeyRef).append("\n");
        sb.append("missionCode: ").append(missionCode).append("\n");
        sb.append("lineRef: ").append(lineRef).append("\n");
        sb.append("arrivalPlatform: ").append(arrivalPlatform).append("\n");
        sb.append("expectedArrivalTime: ").append(expectedArrivalTime).append("\n");
        sb.append("expectedDepartureTime: ").append(expectedDepartureTime).append("\n");
        
        return sb.toString();
    }

}
