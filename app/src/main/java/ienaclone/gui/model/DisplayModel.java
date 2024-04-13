package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayModel {
    private final Stop actualStop;
    private ArrayList<Journey> journeys;
    // private final ArrayList<Alerts> alerts;
    private boolean isTestStopChecked;

    public DisplayModel(Stop st) {
        this.actualStop = st;
        this.journeys = new ArrayList<>();
        this.isTestStopChecked = false;
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public Stop getActualStop() {
        return actualStop;
    }

    public boolean isTestStopChecked() {
        return isTestStopChecked;
    }

    public void setJourneys(ArrayList<Journey> journeys) {
        this.journeys = journeys;
    }

    public void setTestStopChecked(boolean value) {
        this.isTestStopChecked = value;
    }

}
