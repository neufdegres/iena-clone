package ienaclone.gui;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.view.AbstractView;
import ienaclone.gui.view.DashboardView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Window extends Application {
    public static Stage main;
    private static AbstractView view;
    // private static 

    @Override
    public void start(Stage primaryStage) throws Exception {
        main = primaryStage;
        main.setTitle("IENA (clone)");
        main.setResizable(false);
        setDashboard();
    }

    public static void setDashboard() {
        var model = new DashboardModel();
        view = new DashboardView(main, new DashboardController(model));
        view.display();
    }

    // public static void openDisplayWindow() {
    //     Stage displayStage = new Stage();
    //     displayStage.setTitle(/* TODO: afficher le nom de la gare + settings */"affichage");

    // }

    public static void main(String[] args) {
        launch();
    }
    
}
