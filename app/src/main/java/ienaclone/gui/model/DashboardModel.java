package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.util.Stop;

public class DashboardModel {
    private ArrayList<Stop> stops;
    private ArrayList<ArrayList<String>> currentDirections;
    private ArrayList<ArrayList<String>> currentPlatforms;
    private Stop currentStop;

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
    }

    public void setCurrentDirections(ArrayList<ArrayList<String>> currentDirections) {
        this.currentDirections = currentDirections;
    }

    public void setCurrentPlatforms(ArrayList<ArrayList<String>> currentPlatforms) {
        this.currentPlatforms = currentPlatforms;
    }

    public void setCurrentStop(Stop currentStop) {
        this.currentStop = currentStop;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public ArrayList<ArrayList<String>> getCurrentDirections() {
        return currentDirections;
    }

    public ArrayList<ArrayList<String>> getCurrentPlatforms() {
        return currentPlatforms;
    }

    public Stop getCurrentStop() {
        return currentStop;
    }

}
