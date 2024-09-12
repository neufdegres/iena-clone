package ienaclone.gui.view;

import java.util.ArrayList;

import javafx.stage.Stage;

import ienaclone.util.Stop;
import ienaclone.util.Journey;

public abstract class DisplayView extends AbstractView {

    public abstract void updateView(Stop stop, ArrayList<Journey> journeys, int difference);

    public abstract void updateClock(String now);

    public abstract void updateWaitingTime(String txt, int pos);

    public abstract Stage getMain();
    
}
