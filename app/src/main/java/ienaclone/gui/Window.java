package ienaclone.gui;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.model.DashboardModel;
import ienaclone.gui.view.AbstractView;
import ienaclone.gui.view.DashboardView;
import ienaclone.gui.view.DisplayView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Window extends Application {
    public static Stage main;
    private static AbstractView view;
    public static DisplayView displayView;

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

    public static void openDisplayWindow(/* envoyer les settings de la requete*/) {
        Stage stage = new Stage();
        stage.setTitle(/* TODO: afficher le nom de la gare + settings */"affichage");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setResizable(false); // pour le moment
        displayView = new DisplayView(stage);
        displayView.display();
    }

    public static void main(String[] args) {
        launch();
    }
    
}
