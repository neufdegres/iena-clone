package ienaclone.gui.model;

import java.util.ArrayList;

import ienaclone.gui.util.DisplayMode;
import ienaclone.gui.util.InfosCircularQueue;
import ienaclone.gui.util.SelectedFilter;
import ienaclone.gui.util.StopDisruptionsClassed;
import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayModel {
    private final Stop actualStop;
    private final ArrayList<Journey> journeys;
    private final ArrayList<Journey> displayedJourneys;
    private final StopDisruptionsClassed disruptions;
    private final InfosCircularQueue infosQueue;
    private final SelectedFilter filter;
    private final DisplayMode mode;
    private final boolean isTestStop;

    public DisplayModel(DisplaySettings st) {
        this.actualStop = st.getSelected();
        this.journeys = new ArrayList<>();
        this.displayedJourneys = new ArrayList<>();
        this.disruptions = new StopDisruptionsClassed();
        this.infosQueue = new InfosCircularQueue();
        this.isTestStop = st.isTest();
        this.filter = new SelectedFilter(st.getFilter()[0], st.getFilter()[1]);
        this.mode = st.getMode();
    }

    public ArrayList<Journey> getJourneys() {
        return journeys;
    }

    public ArrayList<Journey> getXJourneys(int x) {
        var res = new ArrayList<Journey>();

        for (int i=0; i<journeys.size(); i++) {
            res.add(journeys.get(i));
            x--;
            if (x == 0) break;
        }

        return res;
    }

    public ArrayList<Journey> getDisplayedJourneys() {
        return displayedJourneys;
    }

    public StopDisruptionsClassed getDisruptions() {
        return disruptions;
    }

    public InfosCircularQueue getInfosQueue() {
        return infosQueue;
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
