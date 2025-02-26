package ienaclone.gui;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.controller.DisplayController;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.model.DisplaySettings;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DisplayView;
import ienaclone.gui.view.OnPlatformDisplayView;
import ienaclone.prim.JourneysDataLoader;
import ienaclone.prim.JourneysDataLoader.STATUS;
import ienaclone.util.Functions;

public class Window extends Application {
    public static Stage main;
    private static DashboardView dashboardView;
    private static ArrayList<DisplayController> displaysOpen; 
    private static JourneysDataLoader jdl;

    @Override
    public void start(Stage primaryStage) throws Exception {
        main = primaryStage;
        main.setTitle("IENA (clone)");
        main.setResizable(false);
        setDashboard();
        displaysOpen = new ArrayList<>();
        jdl = null;
    }

    public static void setDashboard() {
        var model = new DashboardModel();
        dashboardView = new DashboardView(main, new DashboardController(model));
        dashboardView.display();
    }

    public static void openDisplayWindow(DisplaySettings settings) {
        Stage stage = new Stage();
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setResizable(false); // pour le moment
        
        var model = new DisplayModel(settings);
        var controller = new DisplayController(model);
        DisplayView dv = null;

        switch (settings.getMode()) {
            case ON_PLATFORM_1_TRAIN:
                dv = new OnPlatformDisplayView(stage, controller);
                break;
            case ON_PLATFORM_3_TRAINS:
                break;
            case OUT_OF_PLATFORM:
                break;
        }

        var actual = !displaysOpen.isEmpty() ?
                        displaysOpen.get(0).getActualStop() : null;
        var selected = settings.getSelected();
            
        if (actual == null || !selected.equals(actual)) {
            if (actual != null) {
                closeAllDisplaysOpen();
                jdl.mainServiceStop();
            }
            jdl = new JourneysDataLoader(selected);
        }

        controller.setJdl(jdl);
        controller.setView(dv);

        displaysOpen.add(controller);

        int did = displaysOpen.size();

        controller.setDisplayId(did);

        stage.setOnHiding(event -> controller.onViewClosed());
        dv.display();

        controller.disruptionsPanelTransitionInit();

        Functions.writeLog("[" + did + "] opened !");

        if (!settings.isTest()) {
            if (jdl.getMainServiceStatus() == STATUS.NOT_STARTED) 
                jdl.mainServiceStart();
        }

        controller.startLoading();
 
    }

    private static void closeAllDisplaysOpen() {
        Platform.runLater(() -> {
            var obs = FXCollections.observableArrayList(displaysOpen);
            obs.stream().forEach(c -> {
                c.getView().getMain().close(); 
            });
        });
    }

    public static void notifyDisplayerClosed(DisplayController closed) {
        displaysOpen.remove(closed);    
        if (displaysOpen.isEmpty()) jdl.mainServiceStop();
    }

    public static void main(String[] args) {
        launch();
    }
    
}
