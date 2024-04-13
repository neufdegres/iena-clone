package ienaclone.gui.controller;

import ienaclone.gui.model.DisplayModel;
// import ienaclone.prim.Requests;
// import ienaclone.util.Files;
// import ienaclone.util.Journey;

// import java.util.ArrayList;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class DisplayController {
    private final DisplayModel model;

    private DisplayController(DisplayModel model) {
        this.model = model;
    }

    // TODO : à finir
    public void loadData() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        /* ArrayList<Journey> nextJourneys;
                        if (model.isTestStopChecked()) { // pour tester si pas de trains (la nuit)
                            nextJourneys = Files.loadTestNextJourneysValues();
                        } else {
                            nextJourneys = Requests.getNextJourneys(model.getActualStop().getCode());
                        }

                        model.setJourneys(nextJourneys); */
                        
                        return null;
                    }
                    
                };
                
            } 
        }.start();
    }

    public void displayOnTerminal() {
        var journeys = model.getJourneys();

        int i = 1;
        for (var j : journeys) {
            StringBuilder sb = new StringBuilder();
            sb.append("-----------------PASSAGE ").append(i).append("-----------------\n");
            sb.append("Nom de la mission : ").append(j.getMissionCode()).append("\n");
            sb.append("Direction : ").append(j.getDestinationName()).append("\n");
            sb.append("Quai : ").append(j.getArivalPlatform()).append("\n");
            sb.append("Heure d'arrivée prévue : ").append(j.getExpectedArrivalTime()).append("\n");
            sb.append("Heure d'arrivée réelle : ").append(j.getAimedArrivalTime()).append("\n");
            sb.append("Taille du train : ");
            if (j.getNumberOfTrains() > 0) {
                if (j.getNumberOfTrains() == 1)
                    sb.append("court").append("\n");
                else
                    sb.append("long").append("\n");
            } else {
                sb.append("N/A").append("\n");
            }

        }
    }


}