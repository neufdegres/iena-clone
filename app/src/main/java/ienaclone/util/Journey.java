package ienaclone.util;

public class Journey {
    private int order;
    private int numberOfTrains;
    private boolean vehicleAtStop;
    private String destinationName, destinationDisplay, destinationRef;
    private String journeyRef;
    private String missionCode;
    private String lineRef;
    private String aimedArrivalTime, aimedDepartureTime;
    private String arrivalPlatform;
    private String arrivalStatus, departureStatus;
    private String expectedArrivalTime, expectedDepartureTime;
    private String stopPointName;

    public Journey(JourneyBuilder builder) {
        this.order = builder.order;
        this.numberOfTrains = builder.numberOfTrains;
        this.vehicleAtStop = builder.vehicleAtStop;
        this.destinationName = builder.destinationName != null ? builder.destinationName : "N/A";
        this.destinationDisplay = builder.destinationDisplay != null ? builder.destinationDisplay : "N/A";
        this.destinationRef = builder.destinationRef != null ? builder.destinationRef : "N/A";
        this.journeyRef = builder.journeyRef != null ? builder.journeyRef : "N/A";
        this.missionCode = builder.missionCode != null ? builder.missionCode : "N/A";
        this.lineRef = builder.lineRef != null ? builder.lineRef : "N/A";
        this.aimedArrivalTime = builder.aimedArrivalTime != null ? builder.aimedArrivalTime : "N/A";
        this.aimedDepartureTime = builder.aimedDepartureTime != null ? builder.aimedDepartureTime : "N/A";
        this.arrivalPlatform = builder.arrivalPlatform != null ? builder.arrivalPlatform : "N/A";
        this.arrivalStatus = builder.arrivalStatus != null ? builder.arrivalStatus : "N/A";
        this.departureStatus = builder.departureStatus != null ? builder.departureStatus : "N/A";
        this.expectedArrivalTime = builder.expectedArrivalTime != null ? builder.expectedArrivalTime : "N/A";
        this.expectedDepartureTime = builder.expectedDepartureTime != null ? builder.expectedDepartureTime : "N/A";
        this.stopPointName = builder.stopPointName != null ? builder.stopPointName : "N/A";
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getNumberOfTrains() {
        return numberOfTrains;
    }

    public void setNumberOfTrains(int numberOfTrains) {
        this.numberOfTrains = numberOfTrains;
    }

    public boolean isVehicleAtStop() {
        return vehicleAtStop;
    }

    public void setVehicleAtStop(boolean vehicleAtStop) {
        this.vehicleAtStop = vehicleAtStop;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationDisplay() {
        return destinationDisplay;
    }

    public void setDestinationDisplay(String destinationDisplay) {
        this.destinationDisplay = destinationDisplay;
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

    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    public String getAimedDepartureTime() {
        return aimedDepartureTime;
    }

    public void setAimedDepartureTime(String aimedDepartureTime) {
        this.aimedDepartureTime = aimedDepartureTime;
    }

    public String getArivalPlatform() {
        return arrivalPlatform;
    }

    public void setArrivalPlatform(String arrivalPlatform) {
        this.arrivalPlatform = arrivalPlatform;
    }

    public String getArrivalStatus() {
        return arrivalStatus;
    }

    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    public String getDepartureStatus() {
        return departureStatus;
    }

    public void setDepartureStatus(String departureStatus) {
        this.departureStatus = departureStatus;
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

    public String getStopPointName() {
        return stopPointName;
    }

    public void setStopPointName(String stopPointName) {
        this.stopPointName = stopPointName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("order: ").append(order).append("\n");
        sb.append("numberOfTrains: ").append(numberOfTrains).append("\n");
        sb.append("vehicleAtStop: ").append(vehicleAtStop).append("\n");
        sb.append("destinationName: ").append(destinationName).append("\n");
        sb.append("destinationDisplay: ").append(destinationDisplay).append("\n");
        sb.append("destinationRef: ").append(destinationRef).append("\n");
        sb.append("journeyRef: ").append(journeyRef).append("\n");
        sb.append("missionCode: ").append(missionCode).append("\n");
        sb.append("lineRef: ").append(lineRef).append("\n");
        sb.append("aimedArrivalTime: ").append(aimedArrivalTime).append("\n");
        sb.append("aimedDepartureTime: ").append(aimedDepartureTime).append("\n");
        sb.append("arrivalPlatform: ").append(arrivalPlatform).append("\n");
        sb.append("arrivalStatus: ").append(arrivalStatus).append("\n");
        sb.append("departureStatus: ").append(departureStatus).append("\n");
        sb.append("expectedArrivalTime: ").append(expectedArrivalTime).append("\n");
        sb.append("expectedDepartureTime: ").append(expectedDepartureTime).append("\n");
        sb.append("stopPointName: ").append(stopPointName).append("\n");
        
        return sb.toString();
    }

}
