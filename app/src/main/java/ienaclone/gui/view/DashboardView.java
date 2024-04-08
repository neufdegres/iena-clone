package ienaclone.gui.view;

import ienaclone.gui.controller.DashboardController;
import ienaclone.util.Mode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
// import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
// import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class DashboardView extends AbstractView {
    private final Stage main; 
    private final DashboardController controller;
    private ChoiceBox<String> gareCB;
    private OptionBox optionBox;
    private Button displayButton;

    public DashboardView(Stage main, DashboardController controller) {
        this.main = main;
        this.controller = controller;
        controller.setView(this);
    }

    @Override
    public void display() {
        Label l = new Label("IENA (clone)");
        l.setAlignment(Pos.TOP_CENTER);
        l.setStyle("-fx-font-size: 28pt;");

        // TODO : remplacer par une barre de recherche
        
        Label gare = new Label("Gare ");
        gareCB = new ChoiceBox<String>();
        gareCB.getItems().addAll("chargement en cours");
        gareCB.getSelectionModel().select(0);
        gareCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int nV = (Integer) newValue;
                if (nV > 0) controller.stopSelected(nV-1);
            }
            
        });

        HBox gareBox = new HBox(20);
        gareBox.setAlignment(Pos.CENTER_LEFT);
        gareBox.getChildren().addAll(gare, gareCB);

        Label mode = new Label("Mode");
        ChoiceBox<String> modeCB = new ChoiceBox<String>();
        modeCB.getItems().addAll("------------------------",
            "Tous les trains", "Par direction (N/A)", "Par quai (N/A)");
        modeCB.getSelectionModel().select(0);
        modeCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.charAt(0) == '-') return;
                controller.modeSelected(Mode.getMode(newValue));
            }
            
        });

        HBox modeBox = new HBox(20);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeBox.getChildren().addAll(mode, modeCB);

        Label warning = new Label("Veuillez choisir un mode d'affichage.");
        optionBox = new OptionBox();
        VBox.setVgrow(optionBox, Priority.ALWAYS);
        // optionBox.setBorder(Border.stroke(Paint.valueOf("#4488aa"))); // color
        // optionBox.setAlignment(Pos.CENTER);
        optionBox.getChildren().add(warning);

        VBox body = new VBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);
        // body.setBorder(Border.stroke(Paint.valueOf("#aa4488"))); // color
        body.setPadding(new Insets(20));
        body.getChildren().addAll(gareBox, modeBox, optionBox);

        displayButton = new Button("Afficher");
        displayButton.setDisable(true);
        displayButton.setOnAction(e -> {
            if (!displayButton.isDisabled()) controller.displayPressed();
        });

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        layout.getChildren().addAll(l, body, displayButton);

        // optionBox.setParDirection(new String[]{"Paris Est /\nHausmann - Saint-Lazarre","Meaux"});
        /* optionBox.setParQuai(new String[]{"A  :  E - Terminus",
                                           "B  :  P - vers Meaux",
                                           "C  :  P - vers Paris",
                                           "D  :  E - vers Paris"}); */

        Scene scene = new Scene(layout, 700, 500);
        scene.getStylesheets().add("/ienaclone/gui/view/dashboard.css");
        main.setScene(scene);
        main.show();

        controller.loadStops();
    }

    public ChoiceBox<String> getGareCB() {
        return gareCB;
    }

    public OptionBox getOptionBox() {
        return optionBox;
    }

    public Button getDisplayButton() {
        return displayButton;
    }

    public class OptionBox extends HBox {

        public OptionBox() {
            super(20);
            this.setAlignment(Pos.TOP_LEFT);
            this.setPadding(new Insets(30));
        }

        public void setParDirection(String[] values) {
            Label direction = new Label("Direction");
            direction.setAlignment(Pos.TOP_LEFT);

            VBox choices = new VBox(5);

            ToggleGroup group = new ToggleGroup();
            group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    if (displayButton.isDisabled()) displayButton.setDisable(false);
                }
                
            });

            for(String dir : values) {
                RadioButton dirTmp = new RadioButton(dir);
                dirTmp.setStyle("-fx-font-size: 14pt;");
                dirTmp.setToggleGroup(group);
                choices.getChildren().add(dirTmp);
            }

            this.getChildren().clear();
            this.getChildren().addAll(direction, choices);
        }

        public void setParQuai(String[] values) {
            Label quai = new Label("Quai");
            quai.setAlignment(Pos.TOP_LEFT);

            VBox choices = new VBox(5);

            ToggleGroup group = new ToggleGroup();

            for(String q : values) {
                RadioButton qTmp = new RadioButton(q);
                qTmp.setStyle("-fx-font-size: 14pt;");
                qTmp.setToggleGroup(group);
                choices.getChildren().add(qTmp);
            }

            this.getChildren().clear();
            this.getChildren().addAll(quai, choices);
        }

    }
    
}
