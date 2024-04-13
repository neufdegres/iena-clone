package ienaclone.gui.controller;

import java.util.ArrayList;

import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.view.DashboardView;
import ienaclone.prim.Parcer;
import ienaclone.prim.Requests;
import ienaclone.util.Files;
import ienaclone.util.Journey;
import ienaclone.util.Stop;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceBox;

public class DashboardController {
    public DashboardView view;
    public final DashboardModel model;
    
    public DashboardController(DashboardModel model) {
        this.model = model;
    }

    public void setView(DashboardView view) {
        this.view = view;
    }

    public void stopSelected(int i) {
        Stop selected = model.getStops().get(i);
        model.setCurrentStop(selected);
        System.out.println(selected);
    }
    
    public void testChecked(boolean isChecked) {
        model.setTestStopChecked(isChecked);
        if (isChecked) {
            Stop testSt = new Stop("41010", "Chelles Gournay");
            testSt.getLines().add("P");
            testSt.getLines().add("E");
            model.setCurrentStop(testSt);
            System.out.println(testSt);
        } else {
            model.setCurrentStop(null);
        }
        
    }

    public void modeSelected(String selected) {
        var box = view.getOptionListBox();

        // si aucun passage de train dans les 2 heures qui suivent
        if (model.getCurrentDirections().isEmpty()) {
            box.setNoValues();
            return;
        }

        switch (selected) {
            case "direction":
                var dirsData = model.getCurrentDirections();
                var dirs = new String[dirsData.size()];
                for (int i = 0; i < dirs.length; i++) {
                    dirs[i] = dirsData.get(i).getName();
                }
                box.setValues(dirs);
                break;
            case "platform":
                var quaisData = model.getCurrentPlatforms();
                var quais = new String[quaisData.size()];
                for (int i = 0; i < quais.length; i++) {
                    quais[i] = quaisData.get(i);
                }
                box.setValues(quais);
                break;
            case "mission":
                var missionData = model.getCurrentMissions();
                var mission = new String[missionData.size()];
                for (int i = 0; i < mission.length; i++) {
                    mission[i] = missionData.get(i);
                }
                box.setValues(mission);
                break;
            case "line":
                var lignesData = model.getCurrentLines();
                var lignes = new String[lignesData.size()];
                for (int i = 0; i < lignes.length; i++) {
                    lignes[i] = lignesData.get(i).getName();
                }
                box.setValues(lignes);
                break;
            default: // all
                box.removeValues();
                break;
        }
    }

    // pour le moment, affiche TOUS les passages (même avec le moindre filtre ajouté)
    public void displayPressed() {
        if (model.getCurrentStop() == null) return;
        System.out.println();
        displayOnTerminal();
    }

    public void loadStops() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        var stops = Files.getAllStops();
                        model.setStops(stops);
                        var stopNames = getNameFromStops(stops);

                        ChoiceBox<String> newCB = new ChoiceBox<>();
                        newCB.getItems().add("------------------------");
                        newCB.getSelectionModel().select(0);
                        stopNames.forEach(e -> {
                            newCB.getItems().add(e);
                        });

                        Platform.runLater(() -> view.remplaceGareCB(newCB));

                        return null;
                    }
                    
                };
                
            } 
            
        }.start();
    }

    private ArrayList<String> getNameFromStops(ArrayList<Stop> s) {
        ArrayList<String> res = new ArrayList<>();
        s.forEach(e -> res.add(e.getName()));
        return res;
    }

    public void loadJourneys() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        ArrayList<Journey> nextJourneys;
                        if (model.isTestStopChecked()) { // pour tester si il n'y pas de trains (ex: la nuit)
                            nextJourneys = Files.loadTestNextJourneysValues();
                        } else {
                            nextJourneys = Requests.getNextJourneys(model.getCurrentStop().getCode());
                        }
                        model.setJourneys(nextJourneys);
                        model.setCurrentDirections(Parcer.parseDirectionsFromData(nextJourneys));
                        model.setCurrentPlatforms(Parcer.parsePlatformsFromData(nextJourneys));
                        model.setCurrentMissions(Parcer.parseMissionsFromData(nextJourneys));
                        model.setCurrentLines(Parcer.parseLinesFromData(nextJourneys));

                        Platform.runLater(() -> {
                            view.enableOptionToggles();
                            view.getDisplayButton().setDisable(false);
                        });
                        
                        return null;
                    }
                    
                };
                
            } 
        }.start();
    }

    public void resetCurrentValues() {
        model.setCurrentStop(null);
        model.setJourneys(null);
        model.setCurrentDirections(null);
        model.setCurrentPlatforms(null);
        model.setCurrentMissions(null);
        model.setCurrentLines(null);
    }

    // TODO : temporaire !!
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

            System.out.println(sb.toString());
            i++;
        }
    }
}
