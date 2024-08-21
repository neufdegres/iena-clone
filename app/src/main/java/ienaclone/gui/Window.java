package ienaclone.gui;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.stage.Stage;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.controller.DisplayController;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.model.DisplaySettings;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DisplayView;
import ienaclone.gui.view.OnPlatformDisplayView;

public class Window extends Application {
    public static Stage main;
    private static DashboardView dashboardView;
    private static ArrayList<DisplayView> displaysOpen; 

    @Override
    public void start(Stage primaryStage) throws Exception {
        main = primaryStage;
        main.setTitle("IENA (clone)");
        main.setResizable(false);
        setDashboard();
        displaysOpen = new ArrayList<>();
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
        controller.setView(dv);
        dv.display();
        displaysOpen.add(dv);
    }

    public static void main(String[] args) {
        launch();
    }
    
}
