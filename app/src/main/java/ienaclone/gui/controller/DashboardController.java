package ienaclone.gui.controller;

import java.util.ArrayList;

import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.view.DashboardView;
import ienaclone.util.Files;
import ienaclone.util.Mode;
import ienaclone.util.Stop;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

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
    }

    public void modeSelected(Mode selected) {
        // TODO : rajouer la data

        switch (selected) {
            case PAR_DIRECTION :
                view.getOptionBox().setParDirection(new String[]{"Paris Est /\nHausmann - Saint-Lazarre",
                                                                 "Meaux"});
                view.getDisplayButton().setDisable(true);
                break;
            case PAR_QUAI :
                view.getOptionBox().setParQuai(new String[]{"A  :  E - Terminus",
                                                            "B  :  P - vers Meaux",
                                                            "C  :  P - vers Paris",
                                                            "D  :  E - vers Paris"});
                view.getDisplayButton().setDisable(true);
                break;
            default:
                // TODO : griser le fond
                view.getOptionBox().getChildren().clear();
                view.getDisplayButton().setDisable(false);
                break;
        }
    }

    public void displayPressed() {
        if (model.getCurrentStop() == null) return;
        System.out.println(model.getCurrentStop());
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

                        var cb = view.getGareCB();
                        
                        Platform.runLater(() -> {
                            cb.getItems().clear();
                            cb.getItems().add("------------------------");
                            cb.getSelectionModel().select(0);
                        });
                            
                        stopNames.forEach(e -> {
                            Platform.runLater(() -> cb.getItems().add(e));
                        });

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
}
