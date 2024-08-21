package ienaclone.gui.view;

import java.util.ArrayList;
import java.time.LocalTime;

import ienaclone.util.Journey;
import javafx.stage.Stage;

public abstract class DisplayView extends AbstractView {

    public abstract void updateView(ArrayList<Journey> journeys);

    public abstract void updateClock(LocalTime now);

    public abstract void updateWaitingTime(String txt, int pos);

    public abstract Stage getMain();
    
}
