package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.gui.controller.util.DisplayMode;
import ienaclone.gui.controller.util.SelectedFilter;
import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayModel {
    private final Stop actualStop;
    private final ArrayList<Journey> journeys;
    // TODO : private final ArrayList<Alerts> alerts;
    private final SelectedFilter filter;
    private final DisplayMode mode;
    private final boolean isTestStop;

    public DisplayModel(DisplaySettings st) {
        this.actualStop = st.getSelected();
        this.journeys = new ArrayList<>();
        this.isTestStop = st.isTest();
        this.filter = new SelectedFilter(st.getFilter()[0], st.getFilter()[1]);
        this.mode = st.getMode();
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public Stop getActualStop() {
        return actualStop;
    }

    public SelectedFilter getFilter() {
        return filter;
    }

    public DisplayMode getMode() {
        return mode;
    }

    public boolean isTestStop() {
        return isTestStop;
    }

}
