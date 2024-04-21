package ienaclone.gui.view;

import ienaclone.gui.controller.DashboardController;

import java.util.stream.Stream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView extends AbstractView {
    private final Stage main; 
    private final DashboardController controller;
    private ChoiceBox<String> gareCB;
    private FilterBox filterBox;
    private HBox gareBox;
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

        Label gare = new Label("Gare ");
        gareCB = new ChoiceBox<String>();
        gareCB.getItems().add("chargement en cours.........");
        gareCB.getSelectionModel().select(0);

        gareBox = new HBox(20);
        gareBox.setAlignment(Pos.CENTER_LEFT);
        gareBox.getChildren().addAll(gare, gareCB);

        CheckBox testGareCB = new CheckBox("Utiliser des données de test (Chelles Gournay) si aucun passage dans les 2 heures");
        testGareCB.setStyle("-fx-font-size: 12pt;");
        testGareCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                controller.testChecked(newValue);
                if (newValue) {
                    gareBox.setDisable(true);
                    controller.loadJourneys();
                } else {
                    gareBox.setDisable(false);
                    gareCB.getSelectionModel().select(0);
                    filterBox.changeStatus(FilterBox.STATUS.NO_STOP_SELECTED, null);
                }
                
            }
        });

        Label afficher = new Label("Afficher par...");
        l.setStyle("-fx-font-size: 18pt;");

        filterBox = new FilterBox();
 
        VBox body = new VBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(gareBox, testGareCB, afficher, filterBox);

        displayButton = new Button("Afficher");
        displayButton.setDisable(true);
        displayButton.getStyleClass().add("display-button");
        displayButton.setTooltip(new Tooltip("Afficher dans le terminal"));
        displayButton.setOnAction(e -> {
            if (!displayButton.isDisabled()) controller.displayPressed();
        });

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        layout.getChildren().addAll(l, body, displayButton);

        Scene scene = new Scene(layout, 700, 500);
        scene.getStylesheets().add("/ienaclone/gui/view/dashboard.css");
        main.setScene(scene);
        main.show();

        controller.loadStops();
    }

    public ChoiceBox<String> getGareCB() {
        return gareCB;
    }

    public FilterBox getFilterBox() {
        return filterBox;
    }

    public Button getDisplayButton() {
        return displayButton;
    }

    public void remplaceGareCB(ChoiceBox<String> newCB) {
        gareBox.getChildren().remove(1);
        gareBox.getChildren().add(newCB);

        gareCB = newCB;

        gareCB.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int nV = (Integer) newValue;
                int oV = (Integer) oldValue;

                if (nV == oV) return;
                if (nV > 0) {
                    controller.stopSelected(nV-1);
                } else {
                    filterBox.changeStatus(FilterBox.STATUS.NO_STOP_SELECTED, null);
                    controller.resetCurrentValues();
                }
            }
        });
    }

    public class FilterBox extends VBox {
        private ToggleGroup buttonsGroup;
        private OptionListBox optionListBox;

        public enum STATUS {LOADING, NO_STOP_SELECTED, NO_TRAIN, ALL_TRAINS,
                            DATA_SET, NO_INTERNET_CONNEXION, ERROR}

        public FilterBox() {
            // https://stackoverflow.com/questions/15819242/how-to-make-a-button-appear-to-have-been-clicked-or-selected-javafx2
    
            buttonsGroup = new ToggleGroup();

            OptionToggle b1 = new OptionToggle("(Tout)", "all");
            OptionToggle b2 = new OptionToggle("Direction", "direction");
            OptionToggle b3 = new OptionToggle("Quai", "platform");
            OptionToggle b4 = new OptionToggle("Mission", "mission");
            OptionToggle b5 = new OptionToggle("Ligne", "line");

            Stream.of(b1, b2, b3, b4, b5)
                .forEach(x -> {
                    x.setToggleGroup(buttonsGroup);
                    x.getStyleClass().add("option-toggle-button");
                    x.setDisable(true);
            });

            buttonsGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle newToggle) -> {
                    if (newToggle == null) {
                        toggle.setSelected(true);
                    } else {
                        OptionToggle ot = (OptionToggle)newToggle;
                        if (!ot.isDisabled()) controller.modeSelected(ot.getKey());
                    }
            });

            HBox optButtons = new HBox(5);
            optButtons.getChildren().addAll(b1, b2, b3, b4, b5);

            optionListBox = new OptionListBox();
            optionListBox.setDefaultView();


            VBox optContentBox = new VBox();
            VBox.setVgrow(optContentBox, Priority.ALWAYS);
            optContentBox.setAlignment(Pos.CENTER);
            optContentBox.getChildren().add(optionListBox);

            ScrollPane scrollPane = new ScrollPane(optContentBox);

            this.setPadding(new Insets(0,10,0,10));
            VBox.setVgrow(this, Priority.ALWAYS);
            this.getChildren().addAll(optButtons, scrollPane);
        }
    
        public ToggleGroup getButtonsGroup() {
            return buttonsGroup;
        }

        public void changeStatus(STATUS st, String[] values) {
            switch (st) {
                case LOADING:
                    disableOptionToggles();
                    optionListBox.setLoadingView();
                    break;
                case NO_STOP_SELECTED:
                    disableOptionToggles();
                    optionListBox.setDefaultView();
                    break;

                case NO_TRAIN: 
                    disableOptionToggles();
                    optionListBox.setNoValues();
                    break;

                case ALL_TRAINS:
                    enableOptionToggles();
                    selectFirstToggle();
                    optionListBox.removeValues();
                    break;

                case DATA_SET:
                    enableOptionToggles();
                    optionListBox.setValues(values);
                    break;

                case NO_INTERNET_CONNEXION:
                    disableOptionToggles();
                    optionListBox.setNoInternetConnexionView();
                    break;

                case ERROR:
                    disableOptionToggles();
                    optionListBox.setErrorView();
                    break;
            }
        }

        private void enableOptionToggles() {
            buttonsGroup.getToggles()
                        .stream()
                        .forEach(
                            e -> {
                                var ot = (OptionToggle)e;
                                if (ot.isDisabled()) ot.setDisable(false);
                            }
                        );
            
        }

        private void disableOptionToggles() {
            buttonsGroup.getToggles()
                        .stream()
                        .forEach(
                            e -> {
                                var ot = (OptionToggle)e;
                                if (!ot.isDisabled()) ot.setDisable(true);
                                if (ot.isSelected()) ot.setSelected(false);
                            }
                        );
        }

        private void selectFirstToggle() {
            if (buttonsGroup.getToggles().size() > 0)
                buttonsGroup.getToggles().get(0).setSelected(true);
        }
    }

    private class OptionListBox extends HBox {

        public OptionListBox() {
            super(20);
            this.setAlignment(Pos.TOP_LEFT);
            this.setPadding(new Insets(10));
            this.getStyleClass().add("option-list-box");
        }

        public void setValues(String[] values) {
            VBox choices = new VBox(5);

            ToggleGroup group = new ToggleGroup();

            for(String q : values) {
                ValueRadioButton qTmp = new ValueRadioButton(q);
                qTmp.setStyle("-fx-font-size: 14pt;");
                qTmp.setToggleGroup(group);
                choices.getChildren().add(qTmp);
            }
            
            group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    var selected = (ValueRadioButton)newValue;
                    controller.filterValueSelected(selected.getKey());
                }

            });

            if (group.getToggles().size() > 0)
                group.selectToggle(group.getToggles().get(0));

            this.getChildren().clear();
            this.getChildren().addAll(choices);
        }

        public void setNoValues() {
            this.getChildren().clear();
            this.getChildren().addAll(new Label("Aucun train de prévu pour les 2 prochaines heures"));
        }

        public void removeValues() {
            this.getChildren().clear();
            this.getChildren().addAll(new Label("Afficher tous les passages"));
        }

        public void setDefaultView() {
            this.getChildren().clear();
            Label warning = new Label("Veuillez choisir d'abord une gare.");
            this.getChildren().add(warning);
        }

        public void setLoadingView() {
            this.getChildren().clear();
            Label warning = new Label("Chargement en cours.........");
            warning.setStyle("-fx-font-style: italic; -fx-font-color: #666666;");
            this.getChildren().add(warning);
        }

        public void setNoInternetConnexionView() {
            this.getChildren().clear();
            Label warning = new Label("Pas de connexion internet.");
            this.getChildren().add(warning);
        }

        public void setErrorView() {
            this.getChildren().clear();
            Label warning = new Label("Erreur lors de l'importation des données.");
            this.getChildren().add(warning);
        }

    }

    private class OptionToggle extends ToggleButton {
        private final String key;

        public OptionToggle(String name, String key) {
            super(name);
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private class ValueRadioButton extends RadioButton {
        private final String key;

        public ValueRadioButton(String nameKey) {
            super(nameKey);
            this.key = nameKey;
        }

        public String getKey() {
            return key;
        }
    }
    
}
