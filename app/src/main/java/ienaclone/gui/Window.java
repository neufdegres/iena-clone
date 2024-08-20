package ienaclone.gui;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.controller.DisplayController;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.model.DisplaySettings;
import ienaclone.gui.view.AbstractView;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DisplayView;
import ienaclone.gui.view.OnPlatformDisplayView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Window extends Application {
    public static Stage main;
    private static AbstractView view;

    @Override
    public void start(Stage primaryStage) throws Exception {
        main = primaryStage;
        main.setTitle("IENA (clone)");
        main.setResizable(false);
        setDashboard();
        // openDisplayWindow();
    }

    public static void setDashboard() {
        var model = new DashboardModel();
        view = new DashboardView(main, new DashboardController(model));
        view.display();
    }

    public static void openDisplayWindow(DisplaySettings settings) {
        Stage stage = new Stage();
        //stage.setTitle(/* TODO: afficher le nom de la gare + settings */"affichage");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setResizable(false); // pour le moment
        var model = new DisplayModel(settings);
        var controller = new DisplayController(model);
        switch (settings.getMode()) {
            case ON_PLATFORM_1_TRAIN:
                view = new OnPlatformDisplayView(stage, controller);
                controller.setView((DisplayView)view);
                view.display();
                break;
            case ON_PLATFORM_3_TRAINS:
                break;
            case OUT_OF_PLATFORM:
                break;
        }
            
    }

    public static void main(String[] args) {
        launch();
    }
    
}
