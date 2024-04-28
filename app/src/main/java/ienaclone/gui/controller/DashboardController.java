package ienaclone.gui.controller;

import java.util.ArrayList;

import ienaclone.gui.Window;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DashboardView.FilterBox;
import ienaclone.prim.Filter;
import ienaclone.prim.Parcer;
import ienaclone.prim.Requests;
import ienaclone.util.AllLinesSingleton;
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
        resetCurrentValues();
        model.setCurrentStop(selected);
        System.out.println(selected);
        loadJourneys();
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
            resetCurrentValues();
        }
        
    }

    public void nextChecked(boolean isChecked) {
        model.setNextStopChecked(isChecked);
    }

    public void modeSelected(String selected) {
        var box = view.getFilterBox();

        switch (selected) {
            case "direction":
                var dirsData = model.getCurrentDirections();
                var dirs = new String[dirsData.size()];
                for (int i = 0; i < dirs.length; i++) {
                    dirs[i] = dirsData.get(i).getName();
                }
                box.changeStatus(FilterBox.STATUS.DATA_SET, dirs);
                model.setSelectedKey(selected);
                break;
            case "platform":
                var quaisData = model.getCurrentPlatforms();
                var quais = new String[quaisData.size()];
                for (int i = 0; i < quais.length; i++) {
                    quais[i] = quaisData.get(i);
                }
                box.changeStatus(FilterBox.STATUS.DATA_SET, quais);
                model.setSelectedKey(selected);
                break;
            case "mission":
                var missionsData = model.getCurrentMissions();
                var missions = new String[missionsData.size()];
                for (int i = 0; i < missions.length; i++) {
                    missions[i] = missionsData.get(i);
                }
                box.changeStatus(FilterBox.STATUS.DATA_SET, missions);
                model.setSelectedKey(selected);
                break;
            case "line":
                var lignesData = model.getCurrentLines();
                var lignes = new String[lignesData.size()];
                for (int i = 0; i < lignes.length; i++) {
                    lignes[i] = lignesData.get(i).getName();
                }
                box.changeStatus(FilterBox.STATUS.DATA_SET, lignes);
                model.setSelectedKey(selected);
                break;
            default: // all
                box.changeStatus(FilterBox.STATUS.ALL_TRAINS, null);
                resetSelectedFilter();
                break;
        }
    }

    public void filterValueSelected(String selected) {
        model.setSelectedValue(selected);
    }

    public void displayPressed() {
        if (model.getCurrentStop() == null) return;
        // System.out.println();
        // displayOnTerminal();
        Window.openDisplayWindow(model.getCurrentStop(),
                model.getSelectedFilter(), model.isTestStopChecked());
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
        view.getFilterBox().changeStatus(FilterBox.STATUS.LOADING, null);
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
                            var rep = Requests.getNextJourneys(model.getCurrentStop().getCode());
                            if (rep.containsKey("data")) {
                                nextJourneys = rep.get("data");
                            } else {
                                Platform.runLater(() -> {
                                    if (rep.containsKey("error_internet")) {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.NO_INTERNET_CONNEXION, null);
                                    } else {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.ERROR, null);
                                    }
                                });
                                return null;
                            }
                        }

                        model.setJourneys(nextJourneys);
                        model.setCurrentDirections(Parcer.parseDirectionsFromData(nextJourneys));
                        model.setCurrentPlatforms(Parcer.parsePlatformsFromData(nextJourneys));
                        model.setCurrentMissions(Parcer.parseMissionsFromData(nextJourneys));
                        model.setCurrentLines(Parcer.parseLinesFromData(nextJourneys));

                        Platform.runLater(() -> {
                            // si aucun passage de train dans les 2 heures qui suivent
                            if (nextJourneys.size() == 0) {
                                view.getFilterBox().changeStatus(FilterBox.STATUS.NO_TRAIN, null);
                            } else {
                                view.getFilterBox().changeStatus(FilterBox.STATUS.ALL_TRAINS, null);
                                view.getDisplayButton().setDisable(false); // TODO
                            }
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
        resetSelectedFilter();
    }

    public void resetSelectedFilter() {
        model.setSelectedKey(null);
        model.setSelectedValue(null);
    }

    // TODO : temporaire
    public ArrayList<Journey> applySelectedFilter() {
        var raw = model.getJourneys();
        String key = model.getSelectedKey();
        String value = model.getSelectedValue();

        if (key == null) return raw;

        switch (key) {
            case "direction":
                System.out.println("Filtre : direction > " + value);
                return (ArrayList<Journey>) Filter.byDirection(raw, value);
            case "platform":
                System.out.println("Filtre : quai > " + value + " \n");
                return (ArrayList<Journey>) Filter.byPlatform(raw, value);
            case "mission":
                System.out.println("Filtre : mission > " + value + " \n");
                return (ArrayList<Journey>) Filter.byMission(raw, value);
            case "line":
                System.out.println("Filtre : ligne > " + value + " \n");
                return (ArrayList<Journey>) Filter.byLine(raw, value);
        }

        return raw;
    }


    // TODO : temporaire !!
    public void displayOnTerminal() {
        ArrayList<Journey> journeys = applySelectedFilter();

        int i = 1;
        for (var j : journeys) {
            StringBuilder sb = new StringBuilder();
            sb.append("-----------------PASSAGE ").append(i).append("-----------------\n");
            sb.append("Ligne : ");
            var ligne = AllLinesSingleton.getInstance().getLineByCode(j.getLineRef());
            if (ligne.isPresent()) {
                sb.append(ligne.get().getName()).append("\n");
            } else {
                sb.append("N/A").append("\n");
            }
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
