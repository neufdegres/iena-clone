package ienaclone.gui.controller;

import java.util.ArrayList;

import ienaclone.gui.Window;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.model.DisplaySettings;
import ienaclone.gui.util.DisplayMode;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DashboardView.FilterBox;
import ienaclone.gui.view.DashboardView.LinesComboBoxItem;
import ienaclone.prim.Filter;
import ienaclone.prim.Parcer;
import ienaclone.prim.Requests;
import ienaclone.util.AllStopsSingleton;
import ienaclone.util.Files;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.Stop;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ComboBox;

public class DashboardController {
    public DashboardView view;
    public final DashboardModel model;
    
    public DashboardController(DashboardModel model) {
        this.model = model;
    }

    public void setView(DashboardView view) {
        this.view = view;
    }

    public DashboardModel getModel() {
        return model;
    }

    public void stopSelected(int i) {
        Stop selected = model.getStops().get(i);
        resetCurrentValues();
        model.setCurrentStop(selected);
        Functions.writeLog("'" + selected.getName() + "' selected !");
        loadJourneys();
    }
    
    public void testChecked(boolean isChecked) {
        model.setTestStopChecked(isChecked);
        if (isChecked) {
            Stop testSt = AllStopsSingleton.getInstance()
                                           .getStopByPointId("68407")
                                           .get();
            model.setCurrentStop(testSt);
            Functions.writeLog("'" + testSt.getName() + "' selected !");
        } else {
            resetCurrentValues();
        }
        
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

    public void displayModeSelected(DisplayMode selected) {
        model.setSelectedDisplayMode(selected);
    }

    public void displayPressed() {
        if (model.getCurrentStop() == null) return;
        var settings = new DisplaySettings();
        settings.setSelected(model.getCurrentStop());
        settings.setFilter(model.getSelectedFilter());
        settings.setTest(model.isTestStopChecked());
        settings.setMode(model.getSelectedDisplayMode());
        Window.openDisplayWindow(settings);
    }

    public void loadStops() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        var stops = AllStopsSingleton.getInstance().getItems();
                        model.setStops(stops);

                        ComboBox<LinesComboBoxItem> newCB = new ComboBox<>();
                        newCB.getItems().add(new LinesComboBoxItem());
                        newCB.getSelectionModel().select(0);
                        stops.forEach(e -> {
                            newCB.getItems().add(new LinesComboBoxItem(e.getName(), e.isRATP(), e.getLines()));
                        });

                        Platform.runLater(() -> view.remplaceGareCB(newCB));

                        return null;
                    }
                    
                };
                
            } 
            
        }.start();
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
                            ArrayList<Journey> allJourneys;
                            var rep = Requests.getNextJourneys(model.getCurrentStop().getPointId());
                            if (rep.containsKey("data")) {
                                allJourneys = rep.get("data");
                            } else {
                                Platform.runLater(() -> {
                                    if (rep.containsKey("error_internet")) {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.NO_INTERNET_CONNEXION, null);
                                    } else if (rep.containsKey("error_apikey")) {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.NO_API_KEY, null);
                                    } else {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.ERROR, null);
                                    }
                                });
                                return null;
                            }

                            nextJourneys = Filter.removeAlreadyPassedTrains(allJourneys, null);
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

}
