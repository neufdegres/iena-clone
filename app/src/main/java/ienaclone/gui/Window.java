package ienaclone.gui;

import ienaclone.gui.view.AbstractView;
import ienaclone.gui.view.DashboardView;
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
    }

    public static void setDashboard() {
        view = new DashboardView(main);
        view.display();
    }

    public static void main(String[] args) {
        launch();
    }
    
}
