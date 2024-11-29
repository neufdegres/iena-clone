package ienaclone.gui.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.IntStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import ienaclone.gui.controller.DisplayController;
import ienaclone.gui.view.OnPlatformDisplayView.StopBox.MODE;
import ienaclone.util.AllLinesSingleton;
import ienaclone.util.AllStopsSingleton;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.Line;
import ienaclone.util.Stop;
import ienaclone.util.Stop.STATUS;
import ienaclone.util.StopDisruption.TYPE;

public class OnPlatformDisplayView extends DisplayView {
    private final Stage main;
    private final DisplayController controller;
    private Label platformNumLabel;
    private InfoBox infoBox;
    private JourneyBox journeyBox;
    private VBox rightBox, subClockBox;

    public OnPlatformDisplayView(Stage main, DisplayController c) {
        this.main = main;
        this.controller = c;
        controller.setView(this);
    }

    @Override
    public void display() {
        // LEFT

        // l'heure

        var clockText = createClock("00 00");

        subClockBox = new VBox(clockText);
        HBox.setMargin(subClockBox, new Insets(8, 0, 20, 10));
        subClockBox.getStyleClass().add("clock-box");

        HBox clockBox = new HBox();
        clockBox.getChildren().addAll(subClockBox);

        // les alertes

        infoBox = new InfoBox();

        ///

        VBox leftBox = new VBox(5);
        infoBox.getStyleClass().add("left-box");
        leftBox.setPrefWidth(1280*0.25);
        // System.out.println(infoBox.layoutXProperty());
        // System.out.println(infoBox.layoutYProperty());
        leftBox.getChildren().addAll(clockBox, infoBox);

        // RIGHT

        // RIGHT-TOP

        Label nextTrain = new Label("Prochain Train");
        nextTrain.getStyleClass().add("next-train-label");

        AnchorPane nextTrainBox = new AnchorPane();
        HBox.setHgrow(nextTrainBox, Priority.ALWAYS);
        nextTrainBox.getChildren().add(nextTrain);

        Label platformLabel = new Label("Voie");
        platformLabel.getStyleClass().add("platform-label");

        platformNumLabel = new Label("51");
        platformNumLabel.getStyleClass().add("platform-num-label");

        HBox platformBox = new HBox(5);
        platformBox.getStyleClass().add("platform-box");
        platformBox.getChildren().addAll(platformLabel, platformNumLabel);

        HBox rightTopBox = new HBox();
        rightTopBox.getChildren().addAll(nextTrainBox, platformBox);

        ///

        rightBox = new VBox(10);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        rightBox.getStyleClass().add("right-box");
        rightBox.getChildren().add(rightTopBox);


        /////////////////////////////

        HBox layout = new HBox();
        layout.getStyleClass().add("layout-box");
        layout.getChildren().addAll(leftBox, rightBox);
        // layout.getChildren().addAll(rightBox);

        Scene scene = new Scene(layout,1280, 720);
        scene.getStylesheets().add("/ienaclone/gui/view/display.css");

        main.setScene(scene);
    }
    
    public Stage getMain() {
        return main;
    }

    public Label getPlatformNumLabel() {
        return platformNumLabel;
    }

    @Override
    public Pane getInfoBox() {
        return infoBox;
    }

    /* INFOS */

    @Override
    public void updateInfosView(TYPE type, String message) {
        // TODO :pictogramme
        
        // couleur de fond + headline
        String color = "", headline = "";

        switch (type) {
            case INFORMATION:
                color = "#313131";
                headline = "Information sûreté";
                break;
            case PERTURBATION:
                color = "#e78754";
                headline = "Information travaux";
                break;
            case COMMERCIAL:
                color = "#313131";
                headline = "#e78754";
                break;
        }

        infoBox.setStyle("-fx-background-color:" + color);
        infoBox.getHeadline().setText(headline);

        // message
        infoBox.getContent().setText(message);
    }

    /* JOURNEYS */

    @Override
    public void updateJourneysView(Stop stop, ArrayList<Journey> journeys, int difference) {
        if (journeys.isEmpty()) {
            clearJourneyBox();
            return;
        }

        Journey actual = journeys.get(0);

        switch (actual.getTimeStatus()) {
            case UNKNOWN:
            case ON_TIME:
                if (actual.getStopStatus(stop) == Stop.STATUS.TERMINUS) {
                    if (difference < 0) {
                        setTerminusOfJourney(actual, stop);
                        displayOnTerminal(actual);
                    }
                } else {
                    if (difference < 0) {
                        setNewJourney(actual, stop);
                        displayOnTerminal(actual);
                    }
                    var label = controller.getWaitingTimeLabel(actual, stop);
                    updateWaitingTime(label, 0);   
                }
                break;
            case CANCELLED:
                if (difference < 0) {
                    setCancelledJourney(actual, stop);
                    displayOnTerminal(actual);
                } 
                break;
            default:
                break;
        }
    }

    private void setNewJourney(Journey actual, Stop stop) {
        // nom de la voie
        platformNumLabel.setText(actual.getPlatform().orElse("N/A"));

        journeyBox = new JourneyBox(JourneyBox.MODE.NORMAL);

        // pictogramme
        var fn = "icon/" + actual.getLine().map(l -> l.getName()).orElse("0") + ".png";
        Image img = new Image(DisplayView.class.getResourceAsStream(fn)); 
        journeyBox.getLineIconView().setImage(img);

        // destination
        var dest = Functions.raccourcir(
                actual.getDestination().map(d -> d.getName()).orElse("Non renseigné")
                , 19);
        journeyBox.getDestination().setText(dest);

        // destination tag
        var destTag = getDestinationTag(actual, stop);
        journeyBox.getDestinationTag().setText(destTag);

        // code mission
        journeyBox.getMission().setText(actual.getMission().orElse("XXXX"));

        // liste des gares
        var stops = actual.getNextStations();
        var stopsBox = journeyBox.getAllStopsBox();

        if (stops.isEmpty()) return;

        boolean currentlyInParis = false;

        var color = actual.getLine().map(l -> l.getColor()).orElse("797979");

        int len = stops.size();

        int idx = IntStream.range(0, len)
                            .filter(i -> stops.get(i).getKey().equals(stop))
                            .findFirst()
                            .orElse(-1);  // TODO : erreur si -1
                        
        for (int i=idx+1; i<len; i++) {
            var stData = stops.get(i);
            var st = stData.getKey();

            var status = stData.getValue();

            if (status == STATUS.SKIPPED) continue;

            var name = st.getName();

            if (stopsBox.getStopCount() == 0) { // le premier item
                if (st.isParis()) {
                    stopsBox.addStop(new StopBox("", color, MODE.PARIS_TOP_DEBUT));
                    currentlyInParis = true;
                } else {
                    stopsBox.addStop(new StopBox("", color, MODE.NORMAL_DEBUT));
                }
            } 

            if (status == STATUS.TERMINUS) { // le terminus
                if (currentlyInParis) {
                    stopsBox.addStop(new StopBox(name, color, MODE.PARIS_BOTTOM_TERMINUS));
                } else if (st.isParis()) {
                    stopsBox.addStop(new StopBox("", color, MODE.PARIS_TOP_MIDDLE));
                    stopsBox.addStop(new StopBox(name, color, MODE.PARIS_BOTTOM_TERMINUS));
                } else {
                    stopsBox.addStop(new StopBox(name, color, MODE.NORMAL_TERMINUS));
                }

            } else { 
                if (currentlyInParis) {
                    if (stops.get(i+1).getKey().isParis()) {
                        stopsBox.addStop(new StopBox(name, color, MODE.PARIS_MIDDLE));
                    } else {
                        stopsBox.addStop(new StopBox(name, color, MODE.PARIS_BOTTOM_MIDDLE));
                        currentlyInParis = false;
                    }
                } else {
                    if (st.isParis()) {
                        stopsBox.addStop(new StopBox("", color, MODE.PARIS_TOP_MIDDLE));
                        stopsBox.addStop(new StopBox(name, color, MODE.PARIS_MIDDLE));
                        currentlyInParis = true;
                    } else {
                        stopsBox.addStop(new StopBox(name, color, MODE.NORMAL_MIDDLE));
                    }
                    
                }
            }
        }

        int nbPages = journeyBox.getAllStopsBox().getPages().size();
        journeyBox.getPagesCount().setText("Page\n1/" + nbPages);

        VBox.setVgrow(journeyBox, Priority.ALWAYS);

        // TODO : ajouter une transition (fade) 
        if (rightBox.getChildren().size() > 1) rightBox.getChildren().remove(1);
        rightBox.getChildren().add(journeyBox);
    }

    private void setCancelledJourney(Journey actual, Stop stop) {
        // nom de la voie
        platformNumLabel.setText(actual.getPlatform().orElse("N/A"));

        journeyBox = new JourneyBox(JourneyBox.MODE.CANCELLED);

        // pictogramme
        var fn = "icon/" + actual.getLine().map(l -> l.getName()).orElse("0") + ".png";
        Image img = new Image(DisplayView.class.getResourceAsStream(fn)); 
        journeyBox.getLineIconView().setImage(img);

        // destination
        var dest = Functions.raccourcir(
                actual.getDestination().map(d -> d.getName()).orElse("Non renseigné")
                , 19);
        journeyBox.getDestination().setText(dest);

        // destination tag
        var destTag = getDestinationTag(actual, stop);
        journeyBox.getDestinationTag().setText(destTag);

        // code mission
        journeyBox.getMission().setText(actual.getMission().orElse("XXXX"));

        VBox.setVgrow(journeyBox, Priority.ALWAYS);

        // TODO : ajouter une transition (fade) 
        if (rightBox.getChildren().size() > 1) rightBox.getChildren().remove(1);
        rightBox.getChildren().add(journeyBox);
    }

    private void setTerminusOfJourney(Journey actual, Stop stop) {
        // nom de la voie
        platformNumLabel.setText(actual.getPlatform().orElse("N/A"));

    
        journeyBox = new JourneyBox(JourneyBox.MODE.TERMINUS);

        VBox.setVgrow(journeyBox, Priority.ALWAYS);

        // TODO : ajouter une transition (fade) 
        if (rightBox.getChildren().size() > 1) rightBox.getChildren().remove(1);
        rightBox.getChildren().add(journeyBox);
    }

    private void clearJourneyBox() {
        rightBox.getChildren().clear();
    }

    private String getDestinationTag(Journey journey, Stop stop) {
        var path = journey.getNextStations();

        int len = path.size();

        int idx = IntStream.range(0, len)
                            .filter(i -> path.get(i).getKey().equals(stop))
                            .findFirst()
                            .orElse(-1);  // TODO : erreur si -1

        var subPath = path.subList(idx+1, len);

        int subLen = subPath.size();

        if (subPath.isEmpty()) return "";

        var allStops = AllStopsSingleton.getInstance();
        var allLines = AllLinesSingleton.getInstance();

        var dest = subPath.get(subLen-1);

        // via Paris

        if (!stop.isParis()
                && subPath.stream()
                          .anyMatch(st -> st.getKey().isParis())
                && !dest.getKey().isParis()) {

            return "via Paris";
        }

        // via Orly

        // TODO : ajouter Antony (RER B)

        // Pont de Rungis
        Stop orlyC = allStops.getStopByCode("41326").get();

        Line c = allLines.getLineByName("C").get();

        if (c.equals(journey.getLine().orElse(null))
                    && subPath.stream()
                              .anyMatch(st -> st.getKey().equals(orlyC))) {

            return "via Orly";
        }

        // via Evry Courcouronnes
        Stop evryCour = allStops.getStopByCode("41346").get();

        Line d = allLines.getLineByName("D").get();

        if (d.equals(journey.getLine().orElse(null))
                    && subPath.stream()
                              .anyMatch(st -> st.getKey().equals(evryCour))) {

            return "via Évry C.";
        }

        // >> Direct

        if (!stop.isParis()
                && subLen > 2
                && dest.getKey().isParis()
                && subPath.subList(0, subLen-2)
                          .stream()
                          .allMatch(st -> st.getValue() == STATUS.SKIPPED)) {

            return ">> Direct";
        }

        return "";
    }

    private void displayOnTerminal(Journey j) {
        int displayId = controller.getDisplayId();

        Functions.writeLog("\n----[" + displayId + "] PROCHAIN PASSAGE-----");

        StringBuilder sb = new StringBuilder();
        sb.append("Ref : ").append(j.getRef()).append("\n");
        sb.append("Ligne : ");
        sb.append(j.getLine().map(line -> line.getName()).orElse("N/A")).append("\n");
        sb.append("Nom de la mission : ").append(j.getMission().orElse("N/A")).append("\n");
        sb.append("Direction : ").append(j.getDestination().orElse(new Stop()).getName()).append("\n");
        sb.append("Quai : ").append(j.getPlatform().orElse("N/A")).append("\n");
        if (j.getExpectedArrivalTime().isPresent() || j.getExpectedDepartureTime().isPresent()) {
            sb.append("Heure d'arrivée estimée : ");
            sb.append(j.getExpectedArrivalTime().map(time -> time.toString()).orElse("N/A")).append("\n");
            sb.append("Heure de départ estimée : ");
            sb.append(j.getExpectedDepartureTime().map(time -> time.toString()).orElse("N/A")).append("\n");
        } else {
            sb.append("Heure d'arrivée visée : ");
            sb.append(j.getAimedArrivalTime().map(time -> time.toString()).orElse("N/A")).append("\n");
            sb.append("Heure de départ visée : ");
            sb.append(j.getAimedDepartureTime().map(time -> time.toString()).orElse("N/A")).append("\n");
        }
        System.out.println(sb.toString());
    }

    @Override
    public void updateWaitingTime(String text, int pos) {
        journeyBox.getWaitingTime().setText(text);
    }

    /* CLOCK */

    private TextFlow createClock(String text) {
        if (text.length() != 5) return new TextFlow();
        TextFlow res = new TextFlow();

        Text h = new Text(text.substring(0, 2));
        Text m = new Text(text.substring(3, 5));
        Text sep = new Text(":");
        if (text.charAt(2) == ' ') sep.setFill(Color.valueOf("eee"));

        res.getChildren().addAll(h, sep, m);
        res.getStyleClass().add("clock-text");
        return res;
    }

    @Override
    public void updateClock(String now) {
        var newClock = createClock(now);
        subClockBox.getChildren().clear();
        subClockBox.getChildren().add(newClock);
    }


    /***** INNER CLASSES *****/

    class InfoBox extends VBox {
        private Label headline, content;

        public InfoBox() {
            headline = new Label("Information sûreté");
            headline.getStyleClass().add("alert-headline-label");

            HBox headlineBox = new HBox();
            headline.getStyleClass().add("alert-headline-box");
            headlineBox.getChildren().addAll(/* le pictogramme, */headline);

            String exAlert = "Pour votre sécurité, nous vous invitons à ne laisser aucun " +
                                "bagage sans surveillance. Veuillez nous signaler tout colis " +
                                "ou bagage qui vous paraitrait abandonné. Merci de votre vigilance";
            content = new Label(exAlert);
            content.getStyleClass().add("alert-content-label");

            this.setStyle("-fx-background-color:#313131");
            // this.setStyle(Color.valueOf("#313131"));
            this.getStyleClass().add("alert-box");
            this.getChildren().addAll(headlineBox, content);
        }

        public Label getHeadline() {
            return headline;
        }

        public Label getContent() {
            return content;
        }
    }

    class JourneyBox extends VBox {
        private ImageView lineIconView;
        private Label pagesCount, destination, destinationTag, waitingTime, mission;
        private AllStopsBox allStopsBox;


        public enum MODE {NORMAL, TERMINUS, CANCELLED}

        public JourneyBox(MODE mode) {
            lineIconView = null;
            pagesCount = null;
            destination = null;
            destinationTag = null;
            waitingTime = null;
            mission = null;
            allStopsBox = null;

            switch (mode) {
                case NORMAL:
                    normalMode();
                    break;
                case TERMINUS:
                    terminusMode();
                    break;
                case CANCELLED:
                    cancelledMode();
                    break;
            }
        }

        private void normalMode() {
            Label trainLen = new Label ("Train Court");
            trainLen.getStyleClass().add("train-length");

            // LEFT

            Image lineIcon = new Image( 
                DisplayView.class.getResourceAsStream("icon/0.png")); 
            
            lineIconView = new ImageView();
            lineIconView.setImage(lineIcon);
            lineIconView.setPreserveRatio(true);
            lineIconView.setFitHeight(80);

            pagesCount = new Label("Page\n1/1");
            pagesCount.getStyleClass().add("pages-label");

            VBox pagesBox = new VBox(pagesCount);
            pagesBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pagesBox, Priority.ALWAYS);

            VBox dataLeftBox = new VBox(8);
            dataLeftBox.getChildren().addAll(lineIconView, pagesBox);

            // CENTER

            destination = new Label("Destination");
            destination.getStyleClass().add("destination-label");

            destinationTag = new Label("test");
            HBox.setMargin(destinationTag, new Insets(0, 0, 3, 0));
            destinationTag.getStyleClass().add("destination-tag-label");

            HBox destinationPane = new HBox(10);
            destinationPane.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setHgrow(destinationPane, Priority.ALWAYS);
            destinationPane.getChildren().addAll(destination, destinationTag);

            waitingTime = new Label("0 min");
            waitingTime.getStyleClass().add("status-label");

            HBox destStatBox = new HBox();
            destStatBox.getStyleClass().add("dest-stat-box");
            destStatBox.getChildren().addAll(destinationPane, waitingTime);


            mission = new Label("XXXX");
            mission.getStyleClass().add("mission-label");

            Label dessert = new Label("Dessert");
            dessert.getStyleClass().add("dessert-label");

            VBox dessertBox = new VBox();
            dessertBox.setAlignment(Pos.CENTER);
            dessertBox.getChildren().add(dessert);

            HBox missDessBox = new HBox(10);
            missDessBox.getChildren().addAll(mission, dessertBox);

            // NEXT STATIONS

            allStopsBox = new AllStopsBox();

            VBox dataCenterBox = new VBox();
            HBox.setHgrow(dataCenterBox, Priority.ALWAYS);
            dataCenterBox.getChildren().addAll(destStatBox, missDessBox, allStopsBox);


            ///////

            HBox dataBox = new HBox(12);
            VBox.setVgrow(dataBox, Priority.ALWAYS);
            dataBox.getChildren().addAll(dataLeftBox, dataCenterBox);
            dataBox.getStyleClass().add("data-box");

            VBox backgroundBox = new VBox();
            VBox.setVgrow(backgroundBox, Priority.ALWAYS);
            backgroundBox.getStyleClass().add("background-box");
            backgroundBox.getChildren().add(dataBox);

            this.setAlignment(Pos.TOP_RIGHT);
            this.getStyleClass().add("journey-box");
            this.getChildren().addAll(trainLen, backgroundBox);
        }

        private void terminusMode() {
            Label trainLen = new Label ("Train Court");
            trainLen.getStyleClass().add("train-length");
            

            Image icon = new Image( 
                DisplayView.class.getResourceAsStream("forbidden.png")); 
            
            ImageView iconImageView = new ImageView();
            iconImageView.setImage(icon);
            iconImageView.setPreserveRatio(true);
            iconImageView.setFitHeight(200);

            Label title = new Label("Terminus");
            title.getStyleClass().add("title-terminus-label");
            Label desc = new Label("Ce train ne prend\npas de voyageurs");
            desc.getStyleClass().add("desc-terminus-label");

            VBox textBox = new VBox(20);
            textBox.setAlignment(Pos.CENTER_LEFT);
            textBox.getChildren().addAll(title, desc);

            HBox contentBox = new HBox(30);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().addAll(iconImageView, textBox);

            ///////

            HBox dataBox = new HBox();
            VBox.setVgrow(dataBox, Priority.ALWAYS);
            dataBox.setAlignment(Pos.CENTER);
            dataBox.getStyleClass().add("data-box");
            dataBox.getChildren().addAll(contentBox);


            VBox backgroundBox = new VBox();
            VBox.setVgrow(backgroundBox, Priority.ALWAYS);
            // backgroundBox.setAlignment(Pos.CENTER);
            backgroundBox.getStyleClass().add("background-box");
            backgroundBox.getChildren().add(dataBox);

            this.setAlignment(Pos.TOP_RIGHT);
            this.getStyleClass().add("journey-box");
            this.getChildren().addAll(trainLen, backgroundBox);

        }

        private void cancelledMode() {
            Label trainLen = new Label ("Train Court");
            trainLen.getStyleClass().add("train-length");

            // LEFT

            Image lineIcon = new Image( 
                DisplayView.class.getResourceAsStream("icon/0.png")); 
            
            lineIconView = new ImageView();
            lineIconView.setImage(lineIcon);
            lineIconView.setPreserveRatio(true);
            lineIconView.setFitHeight(80);

            pagesCount = new Label("Page\n1/1");
            pagesCount.getStyleClass().add("pages-label");

            VBox pagesBox = new VBox(pagesCount);
            pagesBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pagesBox, Priority.ALWAYS);

            VBox dataLeftBox = new VBox(8);
            dataLeftBox.getChildren().addAll(lineIconView, pagesBox);

            // CENTER

            destination = new Label("Destination");
            destination.getStyleClass().addAll("destination-label", "cancelled-label");

            destinationTag = new Label("test");
            HBox.setMargin(destinationTag, new Insets(0, 0, 3, 0));
            destinationTag.getStyleClass().add("destination-tag-label");

            HBox destinationPane = new HBox(10);
            destinationPane.setAlignment(Pos.BOTTOM_LEFT);
            HBox.setHgrow(destinationPane, Priority.ALWAYS);
            destinationPane.getChildren().addAll(destination, destinationTag);

            waitingTime = new Label("supprimé");
            waitingTime.getStyleClass().add("status-label");

            HBox destStatBox = new HBox();
            destStatBox.getStyleClass().add("dest-stat-box");
            destStatBox.getChildren().addAll(destinationPane, waitingTime);


            mission = new Label("XXXX");
            mission.getStyleClass().addAll("mission-label", "cancelled-label");

            // Label dessert = new Label("Dessert");
            // // dessert.getStyleClass().add("dessert-label");

            // VBox dessertBox = new VBox();
            // dessertBox.setAlignment(Pos.CENTER);
            // dessertBox.getChildren().add(dessert);

            HBox missDessBox = new HBox(10);
            // missDessBox.getChildren().addAll(mission, dessertBox);
            missDessBox.getChildren().add(mission);

            // NEXT STATIONS

            // allStopsBox = new AllStopsBox();

            VBox dataCenterBox = new VBox();
            HBox.setHgrow(dataCenterBox, Priority.ALWAYS);
            // dataCenterBox.getChildren().addAll(destStatBox, missDessBox, allStopsBox);
            dataCenterBox.getChildren().addAll(destStatBox, missDessBox);


            ///////

            HBox dataBox = new HBox(12);
            VBox.setVgrow(dataBox, Priority.ALWAYS);
            dataBox.getChildren().addAll(dataLeftBox, dataCenterBox);
            // dataBox.getChildren().add(dataLeftBox);
            dataBox.getStyleClass().add("data-box");

            VBox backgroundBox = new VBox();
            VBox.setVgrow(backgroundBox, Priority.ALWAYS);
            backgroundBox.getStyleClass().add("background-box");
            backgroundBox.getChildren().add(dataBox);

            this.setAlignment(Pos.TOP_RIGHT);
            this.getStyleClass().add("journey-box");
            this.getChildren().addAll(trainLen, backgroundBox);
            // this.getChildren().add(backgroundBox);
            
        }

        public ImageView getLineIconView() {
            return lineIconView;
        }

        public Label getPagesCount() {
            return pagesCount;
        }

        public Label getDestination() {
            return destination;
        }

        public Label getDestinationTag() {
            return destinationTag;
        }

        public Label getWaitingTime() {
            return waitingTime;
        }

        public Label getMission() {
            return mission;
        }

        public AllStopsBox getAllStopsBox() {
            return allStopsBox;
        }
    
    }

    class AllStopsBox extends HBox {
        private final LinkedList<StopsPageBox> pages;
        private int currentPage, stopCount;

        public AllStopsBox() {
            this.pages = new LinkedList<>();
            this.currentPage = 1;
            this.stopCount = 0;

            var firstPage = new StopsPageBox();

            pages.add(firstPage);
            this.getChildren().add(firstPage);
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public LinkedList<StopsPageBox> getPages() {
            return pages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getStopCount() {
            return stopCount;
        }

        public void addStop(StopBox st) {
            var lastPage = pages.getLast();
            if (!lastPage.isFull()) {
                lastPage.addStop(st);
            } else {
                var newPage = new StopsPageBox();
                newPage.getChildren().add(st);
                pages.add(newPage);
            }
            stopCount++;
        }
    }

    class StopsPageBox extends HBox {
        private VBox left, right;

        public StopsPageBox() {
            super(15);
            left = new VBox();
            right = new VBox();

            this.getChildren().addAll(left, right);
        }

        public boolean isFull() {
            return right.getChildren().size() >= 10;
        }

        // on admet que part2 n'est pas rempli
        public void addStop(StopBox st) {
            if (left.getChildren().size() > 10) {
                right.getChildren().add(st);
            } else {
                left.getChildren().add(st);
            }
        }
    }

    class StopBox extends HBox {
        private String name;
        private String color;

        public enum MODE {NORMAL_DEBUT, NORMAL_MIDDLE, NORMAL_TERMINUS,
                        PARIS_TOP_DEBUT, PARIS_TOP_MIDDLE, PARIS_MIDDLE,
                        PARIS_BOTTOM_MIDDLE, PARIS_BOTTOM_TERMINUS}

        public StopBox(String name, String color, MODE mode) {
            this.name = name;
            this.color = color;

            Group leftDesign = getBackgroundDesign(mode);

            VBox shapeBox = new VBox();
            shapeBox.getChildren().add(leftDesign);

            this.getChildren().addAll(shapeBox);
        }

        private Group getBackgroundDesign(MODE mode) {
            Group res = new Group(), line = null, bg = null;
            switch (mode) {
                case NORMAL_DEBUT:
                    bg = getTransparentBg();
                    line = getDebutLineDesign(false);
                    break;
                case NORMAL_MIDDLE:
                    bg = getTransparentBg();
                    line = getMiddleLineDesign(false);
                    break;
                case NORMAL_TERMINUS:
                    bg = getTransparentBg();
                    line = getTerminusLineDesign();
                    break;
                case PARIS_TOP_DEBUT: // celui tt en haut
                    bg = getParisTopBg();
                    line = getDebutLineDesign(true);
                    break;
                case PARIS_TOP_MIDDLE:
                    bg = getParisTopBg();
                    line = getMiddleLineDesign(true);
                    break;
                case PARIS_MIDDLE:
                    bg = getParisMiddleBg();
                    line = getMiddleLineDesign(false);
                    break;
                case PARIS_BOTTOM_MIDDLE: 
                    bg = getParisBottomBg();
                    line = getMiddleLineDesign(false);
                    break;
                case PARIS_BOTTOM_TERMINUS:
                    bg = getParisBottomBg();
                    line = getTerminusLineDesign();
                    break;
            }
            res.getChildren().addAll(bg.getChildren());
            res.getChildren().addAll(line.getChildren());

            // texte
            if (!name.isBlank()) {
                Text nameText = null;
                int limit = 0;
                String styleClass = "";
                
                // TODO : voir si c'est vraiment utile
                if (System.getProperty("os.name").contains("Windows")) {
                    styleClass = "name-label-windows";
                    limit = 30;
                } else {
                    styleClass = "name-label";
                    limit = 19;
                }

                while(true) {
                    var toWrite = Functions.raccourcir(name, limit);
                    nameText = new Text(toWrite);
                    nameText.getStyleClass().add(styleClass);
                    if(nameText.getBoundsInLocal().getWidth() < 126) break;
                    limit--;
                }

                nameText.setX(40);
                nameText.setY(30);
                nameText.setFill(Color.valueOf("eeeeee"));
                res.getChildren().add(nameText);
                
            }

            return res;
        }

        private Group getDebutLineDesign(boolean isParis) {
            Rectangle r1 = new Rectangle();
            r1.setX(13.0f);
            r1.setY(0);
            r1.setWidth(12.0f);
            if (isParis) r1.setHeight(40.0f); // TODO !!!!!
            else r1.setHeight(10.0f);
            r1.setFill(Color.valueOf(color));

            Rectangle r2 = new Rectangle();
            r2.setX(13.0f);
            r2.setY(2.0f);
            r2.setWidth(12.0f);
            r2.setHeight(2.0f);
            r2.setFill(Color.valueOf("1f266c"));

            Rectangle r3 = new Rectangle();
            r3.setX(13.0f);
            r3.setY(6.0f);
            r3.setWidth(12.0f);
            r3.setHeight(2.0f);
            r3.setFill(Color.valueOf("1f266c"));

            return new Group(r1, r2, r3);
        }

        private Group getMiddleLineDesign(boolean isParis) {
            Rectangle r1 = new Rectangle();
            r1.setX(13.0f);
            r1.setY(0);
            r1.setWidth(12.0f);
            r1.setHeight(40.0f);
            r1.setFill(Color.valueOf(color));

            if (isParis) return new Group(r1);

            Circle c1 = new Circle();
            c1.setCenterX(19.0f);
            c1.setCenterY(20.0f);
            c1.setRadius(10.0f);
            c1.setFill(Color.valueOf(color));

            Circle c2 = new Circle();
            c2.setCenterX(19.0f);
            c2.setCenterY(20.0f);
            c2.setRadius(7.0f);
            c2.setFill(Color.valueOf("1f266c"));

            return new Group(r1,c1,c2);
        }

        private Group getTerminusLineDesign() {
            Rectangle r1 = new Rectangle();
            r1.setX(13.0f);
            r1.setY(0);
            r1.setWidth(12.0f);
            r1.setHeight(30.0f);
            r1.setFill(Color.valueOf(color));

            Circle c1 = new Circle();
            c1.setCenterX(19.0f);
            c1.setCenterY(20.0f);
            c1.setRadius(13.0f);
            c1.setFill(Color.valueOf(color));

            Circle c2 = new Circle();
            c2.setCenterX(19.0f);
            c2.setCenterY(20.0f);
            c2.setRadius(7.0f);
            c2.setFill(Color.valueOf("1f266c"));

            return new Group(r1,c1,c2);
        }

        private Group getTransparentBg() {
            Rectangle r1 = new Rectangle();
            r1.setX(0);
            r1.setY(0);
            r1.setWidth(350.0f);
            r1.setHeight(1.0f);
            r1.setFill(Color.TRANSPARENT);

            return new Group(r1);
        }

        private Group getParisTopBg() {
            Rectangle r0 = new Rectangle();
            r0.setX(280.0f);
            r0.setY(5.0f);
            r0.setWidth(70.0f);
            r0.setHeight(35.0f);
            r0.setArcWidth(20.0f);
            r0.setArcHeight(20.0f);
            r0.setFill(Color.valueOf("596ab2"));

            Rectangle r00 = new Rectangle();
            r00.setX(282.0f);
            r00.setY(7.0f);
            r00.setWidth(66.0f);
            r00.setHeight(31.0f);
            r00.setArcWidth(20.0f);
            r00.setArcHeight(20.0f);
            r00.setFill(Color.valueOf("394487"));

            Text t1 = new Text("Paris");
            t1.setX(294.0f);
            t1.setY(22.0f);
            t1.setFill(Color.valueOf("7d83c2"));
            t1.getStyleClass().add("paris-label");

            Rectangle r1 = new Rectangle();
            r1.setX(0);
            r1.setY(25);
            r1.setWidth(350.0f);
            r1.setHeight(15.0f);
            r1.setArcWidth(20.0f);
            r1.setArcHeight(20.0f);
            r1.setFill(Color.valueOf("596ab2"));

            Rectangle r2 = new Rectangle();
            r2.setX(0);
            r2.setY(33.8f);
            r2.setWidth(350.0f);
            r2.setHeight(6.2f);
            r2.setFill(Color.valueOf("596ab2"));

            Rectangle r3 = new Rectangle();
            r3.setX(2.0f);
            r3.setY(27.0f);
            r3.setWidth(346.0f);
            r3.setHeight(11.0f);
            r3.setArcWidth(20.0f);
            r3.setArcHeight(20.0f);
            r3.setFill(Color.valueOf("394487"));
            
            Rectangle r4 = new Rectangle();
            r4.setX(2.0f);
            r4.setY(33.8f);
            r4.setWidth(346.0f);
            r4.setHeight(6.2f);
            r4.setFill(Color.valueOf("394487"));

            return new Group(r0,r00,t1,r1,r2,r3,r4);
        }

        private Group getParisMiddleBg() {
            Rectangle r1 = new Rectangle();
            r1.setX(0);
            r1.setY(0);
            r1.setWidth(350.0f);
            r1.setHeight(40.0f);
            r1.setFill(Color.valueOf("596ab2"));
            
            Rectangle r2 = new Rectangle();
            r2.setX(2.0f);
            r2.setY(0);
            r2.setWidth(346.0f);
            r2.setHeight(40.0f);
            r2.setFill(Color.valueOf("394487"));
            
            return new Group(r1,r2);
        }

        private Group getParisBottomBg() {
            Rectangle r1 = new Rectangle();
            r1.setX(0);
            r1.setY(0);
            r1.setWidth(350.0f);
            r1.setHeight(40.0f);
            r1.setArcWidth(20.0f);
            r1.setArcHeight(20.0f);
            r1.setFill(Color.valueOf("596ab2"));

            Rectangle r2 = new Rectangle();
            r2.setX(0);
            r2.setY(0);
            r2.setWidth(350.0f);
            r2.setHeight(10.0f);
            r2.setFill(Color.valueOf("596ab2"));

            Rectangle r3 = new Rectangle();
            r3.setX(2.0f);
            r3.setY(2.0f);
            r3.setWidth(346.0f);
            r3.setHeight(36.0f);
            r3.setArcWidth(20.0f);
            r3.setArcHeight(20.0f);
            r3.setFill(Color.valueOf("394487"));
            
            Rectangle r4 = new Rectangle();
            r4.setX(2.0f);
            r4.setY(0);
            r4.setWidth(346.0f);
            r4.setHeight(10.0f);
            r4.setFill(Color.valueOf("394487"));
            
            return new Group(r1,r2,r3,r4);
        }

    }

}
