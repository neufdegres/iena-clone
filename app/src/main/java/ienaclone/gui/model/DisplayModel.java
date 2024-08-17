package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayModel {
    private final Stop actualStop;
    private final ArrayList<Journey> journeys;
    // TODO : private final ArrayList<Alerts> alerts;
    private final boolean isTestStop;


    public DisplayModel(Stop st) {
        this.actualStop = st;
        this.journeys = new ArrayList<>();
        this.isTestStop = false;
    }

    public DisplayModel(DisplaySettings st) {
        this.actualStop = st.getSelected();
        this.journeys = new ArrayList<>();
        this.isTestStop = st.isTest();
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public Stop getActualStop() {
        return actualStop;
    }

    public boolean isTestStop() {
        return isTestStop;
    }

}
