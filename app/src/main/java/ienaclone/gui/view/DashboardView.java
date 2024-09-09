package ienaclone.gui.view;

import ienaclone.gui.controller.DashboardController;
import ienaclone.gui.controller.util.DisplayMode;
import ienaclone.util.Functions;

import java.util.stream.Stream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
    private ComboBox<String> gareCB;
    private FilterBox filterBox;
    private HBox gareBox;
    private VBox displayBox;
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
        gareCB = new ComboBox<String>();
        gareCB.getItems().add("chargement en cours.........");
        gareCB.getSelectionModel().select(0);

        gareBox = new HBox(20);
        gareBox.setAlignment(Pos.CENTER_LEFT);
        gareBox.getChildren().addAll(gare, gareCB);

        CheckBox testGareCB = new CheckBox("Utiliser des données pré-chargées (gare de Chelles Gournay)");
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

        Label afficherPar = new Label("Afficher par...");
        l.setStyle("-fx-font-size: 18pt;");

        filterBox = new FilterBox();

        Label modeAffichage = new Label("Mode d'affichage :");
        l.setStyle("-fx-font-size: 18pt;");

        HBox displayValuesBox = new HBox(5);
        displayValuesBox.setPadding(new Insets(10,10,0,10));

        ToggleGroup displayTG = new ToggleGroup();

        var d1CB = new DisplayRadioButton(DisplayMode.OUT_OF_PLATFORM, "\"hors quai\"");
        var d2CB = new DisplayRadioButton(DisplayMode.ON_PLATFORM_1_TRAIN, "quai - 1 train");
        var d3CB = new DisplayRadioButton(DisplayMode.ON_PLATFORM_3_TRAINS, "quai - 3 trains");

        d1CB.setDisable(true);
        d3CB.setDisable(true);

        Stream.of(d1CB, d2CB, d3CB)
            .forEach(d -> {
                d.setStyle("-fx-font-size: 12pt;");
                d.setToggleGroup(displayTG);
                displayValuesBox.getChildren().add(d);
            });

        displayTG.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    var selected = (DisplayRadioButton)newValue;
                    controller.displayModeSelected(selected.getKey());
                }

        });

        if (displayTG.getToggles().size() > 0)
            displayTG.selectToggle(displayTG.getToggles().get(1));

        displayBox = new VBox();

        displayBox.setVisible(false);
        displayBox.getChildren().addAll(modeAffichage, displayValuesBox);
            
 
        VBox body = new VBox(15);
        VBox.setVgrow(body, Priority.ALWAYS);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(gareBox, testGareCB, afficherPar, filterBox, displayBox);

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

        Scene scene = new Scene(layout, 700, 580);
        scene.getStylesheets().add("/ienaclone/gui/view/dashboard.css");
        main.setScene(scene);
        main.show();

        controller.loadStops();
    }

    public ComboBox<String> getGareCB() {
        return gareCB;
    }

    public FilterBox getFilterBox() {
        return filterBox;
    }

    public Button getDisplayButton() {
        return displayButton;
    }

    public void remplaceGareCB(ComboBox<String> newCB) {
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

        gareCB.setVisibleRowCount(12);
    }

    public class FilterBox extends VBox {
        private ToggleGroup buttonsGroup;
        private OptionListBox optionListBox;

        public enum STATUS {LOADING, NO_STOP_SELECTED, NO_TRAIN, ALL_TRAINS,
                            DATA_SET, NO_INTERNET_CONNEXION, NO_API_KEY, ERROR}

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
                    displayBox.setVisible(false);
                    break;
                case NO_STOP_SELECTED:
                    disableOptionToggles();
                    optionListBox.setDefaultView();
                    displayBox.setVisible(false);
                    break;

                case NO_TRAIN: 
                    disableOptionToggles();
                    optionListBox.setNoValues();
                    displayBox.setVisible(false);
                    break;

                case ALL_TRAINS:
                    enableOptionToggles();
                    selectFirstToggle();
                    optionListBox.removeValues();
                    displayBox.setVisible(true);
                    break;

                case DATA_SET:
                    enableOptionToggles();
                    optionListBox.setValues(values);
                    displayBox.setVisible(true);
                    break;

                case NO_INTERNET_CONNEXION:
                    disableOptionToggles();
                    optionListBox.setNoInternetConnexionView();
                    displayBox.setVisible(false);
                    break;

                case NO_API_KEY:
                    disableOptionToggles();
                    optionListBox.setNoApiKeyView();
                    break;

                case ERROR:
                    disableOptionToggles();
                    optionListBox.setErrorView();
                    displayBox.setVisible(false);
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
                    var key = ((OptionToggle)filterBox.getButtonsGroup().getSelectedToggle()).getKey();
                    Functions.writeLog("Filter '" + key + " > " + selected.getKey() + "' set !");
                }

            });

            if (group.getToggles().size() > 0)
                group.selectToggle(group.getToggles().get(0));

            this.getChildren().clear();
            this.getChildren().addAll(choices);
        }

        public void setNoValues() {
            this.getChildren().clear();
            Label warning = new Label("Aucun train de prévu pour les 2 prochaines heures");
            this.getChildren().add(warning);
        }

        public void removeValues() {
            this.getChildren().clear();
            Label warning = new Label("Ne pas filtrer les passages à venir");
            this.getChildren().add(warning);
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

        public void setNoApiKeyView() {
            this.getChildren().clear();
            String msg = "Pas de clé d'API enregistrée : impossible d'obtenir les données des passages.\n"
                         + "Voir le README pour plus d'infos.";
            Label warning = new Label(msg);
            warning.setWrapText(true);
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

    private class DisplayRadioButton extends RadioButton {
        private final DisplayMode key;

        public DisplayRadioButton(DisplayMode nameKey, String text) {
            super(text);
            this.key = nameKey;
        }

        public DisplayMode getKey() {
            return key;
        }
    }
    
}
