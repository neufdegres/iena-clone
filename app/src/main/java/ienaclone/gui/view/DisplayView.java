package ienaclone.gui.view;

import java.util.ArrayList;
import java.time.LocalDateTime;

import ienaclone.util.Journey;
import javafx.stage.Stage;

public abstract class DisplayView extends AbstractView {

    public abstract void updateView(ArrayList<Journey> journeys);

    public abstract void updateTime(LocalDateTime now);

    public abstract Stage getMain();
    
}
