package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.gui.util.DisplayMode;
import ienaclone.util.Journey;
import ienaclone.util.Line;
import ienaclone.util.Stop;

public class DashboardModel {
    private ArrayList<Stop> stops;
    private ArrayList<Journey> journeys;
    private ArrayList<Stop> currentDirections;
    private ArrayList<String> currentPlatforms;
    private ArrayList<String> currentMissions;
    private ArrayList<Line> currentLines;
    private Stop currentStop;
    private boolean isTestStopChecked;
    private DisplayMode selectedDisplayMode;
    private String[] selectedFilter;

    public void setStops(ArrayList<Stop> stops) {
        this.stops = stops;
        this.isTestStopChecked = false;
        this.selectedDisplayMode = DisplayMode.ON_PLATFORM_1_TRAIN;
        this.selectedFilter = new String[2];
    }

    public void setJourneys(ArrayList<Journey> journeys) {
        this.journeys = journeys;
    }

    public void setCurrentDirections(ArrayList<Stop> currentDirections) {
        this.currentDirections = currentDirections;
    }

    public void setCurrentPlatforms(ArrayList<String> currentPlatforms) {
        this.currentPlatforms = currentPlatforms;
    }

    public void setCurrentMissions(ArrayList<String> currentMissions) {
        this.currentMissions = currentMissions;
    }

    public void setCurrentLines(ArrayList<Line> currentLines) {
        this.currentLines = currentLines;
    }

    public void setCurrentStop(Stop currentStop) {
        this.currentStop = currentStop;
    }

    public void setTestStopChecked(boolean value) {
        this.isTestStopChecked = value;
    }

    public void setSelectedKey(String key) {
        this.selectedFilter[0] = key;
    }

    public void setSelectedDisplayMode(DisplayMode selectedDisplayMode) {
        this.selectedDisplayMode = selectedDisplayMode;
    }

    public void setSelectedValue(String value) {
        this.selectedFilter[1] = value;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public ArrayList<Stop> getCurrentDirections() {
        return currentDirections;
    }

    public ArrayList<String> getCurrentPlatforms() {
        return currentPlatforms;
    }

    public ArrayList<String> getCurrentMissions() {
        return currentMissions;
    }

    public ArrayList<Line> getCurrentLines() {
        return currentLines;
    }

    public Stop getCurrentStop() {
        return currentStop;
    }

    public boolean isTestStopChecked() {
        return isTestStopChecked;
    }

    public String[] getSelectedFilter() {
        return selectedFilter;
    }

    public String getSelectedKey() {
        return selectedFilter[0];
    }

    public DisplayMode getSelectedDisplayMode() {
        return selectedDisplayMode;
    }

    public String getSelectedValue() {
        return selectedFilter[1];
    }

}
