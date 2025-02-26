package ienaclone.gui.view;

import java.util.ArrayList;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import ienaclone.util.Stop;
import ienaclone.util.StopDisruption.TYPE;
import ienaclone.util.Journey;

public abstract class DisplayView extends AbstractView {

    public abstract void updateJourneysView(Stop stop, ArrayList<Journey> journeys, int difference);

    public abstract void updateDisruptionView(TYPE type, String message);

    public abstract void updateClock(String now);

    public abstract void updateWaitingTime(String txt, int pos);

    public abstract Stage getMain();

    public abstract Pane getDisruptionsBox(); 
    
}
