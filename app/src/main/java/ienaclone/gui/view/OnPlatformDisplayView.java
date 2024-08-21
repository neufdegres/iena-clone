package ienaclone.gui.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import ienaclone.gui.controller.DisplayController;
import ienaclone.gui.view.OnPlatformDisplayView.StopBox.MODE;
import ienaclone.util.Functions;
import ienaclone.util.Journey;

public class OnPlatformDisplayView extends DisplayView {
    private final Stage main;
    private final DisplayController controller;
    private Label timeLabel, platformNumLabel;
    private JourneyBox journeyBox;

    public OnPlatformDisplayView(Stage main, DisplayController c) {
        this.main = main;
        this.controller = c;
        controller.setView(this);
    }

    @Override
    public void display() {
        // LEFT

        // l'heure

        timeLabel = new Label("00:00");
        timeLabel.getStyleClass().add("time-label");

        VBox timeBox = new VBox();
        VBox.setMargin(timeBox, new Insets(8, 0, 20, 10));
        timeBox.getChildren().add(timeLabel);

        // les alertes

        // TODO: créer une classe spéciale

        Label headline = new Label("Information sûreté");
        headline.getStyleClass().add("alert-headline-label");

        HBox headlineBox = new HBox();
        headline.getStyleClass().add("alert-headline-box");
        headlineBox.getChildren().addAll(/* le pictogramme, */headline);

        String exAlert = "Pour votre sécurité, vous vous invitons à ne laisser aucun " +
                         "bagage sans surveillance. Veuillez nous signaler tout colis " +
                         "ou bagage qui vous paraitrait abandonné. Merci de votre vigilance";
        Label content = new Label(exAlert);
        content.getStyleClass().add("alert-content-label");

        VBox alertBox = new VBox();
        alertBox.getStyleClass().add("alert-box");
        alertBox.getChildren().addAll(headlineBox, content);


        ///

        VBox leftBox = new VBox(5);
        alertBox.getStyleClass().add("left-box");
        leftBox.setPrefWidth(1280*0.25);
        leftBox.getChildren().addAll(timeBox, alertBox);

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

        // RIGHT-BODY

        journeyBox = new JourneyBox();
        VBox.setVgrow(journeyBox, Priority.ALWAYS);

        ///

        VBox rightBox = new VBox(10);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        rightBox.getStyleClass().add("right-box");
        rightBox.getChildren().addAll(rightTopBox, journeyBox);


        /////////////////////////////

        HBox layout = new HBox();
        layout.getStyleClass().add("layout-box");
        layout.getChildren().addAll(leftBox, rightBox);

        Scene scene = new Scene(layout,1280, 720);
        scene.getStylesheets().add("/ienaclone/gui/view/display.css");

        main.setScene(scene);

        controller.firstLoad();
    }
    
    public Stage getMain() {
        return main;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public Label getPlatformNumLabel() {
        return platformNumLabel;
    }

    @Override
    public void updateView(ArrayList<Journey> journeys) {
        Journey actual = journeys.get(0);

        // nom de la voie
        platformNumLabel.setText(actual.getPlatform().orElse("N/A"));

        // TODO : vérifier si terminus

        // pictogramme
        var fn = "icon/" + actual.getLine().map(l -> l.getName()).orElse("0") + ".png";
        Image img = new Image(DisplayView.class.getResourceAsStream(fn)); 
        journeyBox.getLineIconView().setImage(img);

        // destination
        journeyBox.getDestination().setText(
            actual.getDestination().map(d -> d.getName()).orElse("Non renseigné")
        );

        // code mission
        journeyBox.getMission().setText(actual.getMission().orElse("XXXX"));

        // liste des gares
        var stops = actual.getNextStations();
        var stopsBox = journeyBox.getAllStopsBox();

        if (stops.isEmpty()) return;

        boolean currentlyInParis = false;

        var color = actual.getLine().map(l -> l.getColor()).orElse("797979");

        int len = stops.size();

        for (int i=0; i<len; i++) {
            var st = stops.get(i);
            var name = st.getName();

            // System.out.println(name + " " + st.isParis());

            if (i == 0) { // le premier item
                if (st.isParis()) {
                    stopsBox.addStop(new StopBox("", color, MODE.PARIS_TOP_DEBUT));
                    currentlyInParis = true;
                } else {
                    stopsBox.addStop(new StopBox("", color, MODE.NORMAL_DEBUT));
                }
            } 

            if (i+1 == len) { // le terminus
                if (currentlyInParis) {
                    stopsBox.addStop(new StopBox(name, color, MODE.PARIS_BOTTOM_TERMINUS));
                } else {
                    stopsBox.addStop(new StopBox(name, color, MODE.NORMAL_TERMINUS));
                }

            } else { 
                if (currentlyInParis) {
                    if (stops.get(i+1).isParis()) {
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
        
    }

    @Override
    public void updateClock(LocalTime now) {
        var txt = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        timeLabel.setText(txt);
    }

    @Override
    public void updateWaitingTime(String text, int pos) {
        journeyBox.getWaitingTime().setText(text);
    }

    class JourneyBox extends VBox {
        private ImageView lineIconView;
        private Label pagesCount, destination, waitingTime, mission;
        private AllStopsBox allStopsBox;

        public JourneyBox() {
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

            AnchorPane destinationPane = new AnchorPane();
            HBox.setHgrow(destinationPane, Priority.ALWAYS);
            destinationPane.getChildren().add(destination);

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

        public ImageView getLineIconView() {
            return lineIconView;
        }

        public Label getPagesCount() {
            return pagesCount;
        }

        public Label getDestination() {
            return destination;
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
        private int currentPage;

        public AllStopsBox() {
            this.pages = new LinkedList<>();
            this.currentPage = 1;

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

        public void addStop(StopBox st) {
            var lastPage = pages.getLast();
            if (!lastPage.isFull()) {
                lastPage.addStop(st);
            } else {
                var newPage = new StopsPageBox();
                newPage.getChildren().add(st);
                pages.add(newPage);
            }
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
            if (left.getChildren().size() >= 10) {
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

            Group leftDesign = getLeftDesign(mode);

            VBox shapeBox = new VBox();
            shapeBox.getChildren().add(leftDesign);

            this.getChildren().addAll(shapeBox);
        }

        private Group getLeftDesign(MODE mode) {
            Group res = new Group(), line = null, bg = null;
            switch (mode) {
                case NORMAL_DEBUT:
                    bg = getTransparentBg();
                    line = getDebutLineDesign(false);
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case NORMAL_MIDDLE:
                    bg = getTransparentBg();
                    line = getMiddleLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case NORMAL_TERMINUS:
                    bg = getTransparentBg();
                    line = getTerminusLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case PARIS_TOP_DEBUT: // celui tt en haut
                    bg = getParisTopBg();
                    line = getDebutLineDesign(true);
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case PARIS_TOP_MIDDLE:
                    bg = getParisTopBg();
                    line = getMiddleLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case PARIS_MIDDLE:
                    bg = getParisMiddleBg();
                    line = getMiddleLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case PARIS_BOTTOM_MIDDLE:  // ROSA PARKS
                    bg = getParisBottomBg();
                    line = getMiddleLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
                case PARIS_BOTTOM_TERMINUS:
                    bg = getParisBottomBg();
                    line = getTerminusLineDesign();
                    res.getChildren().addAll(bg.getChildren());
                    res.getChildren().addAll(line.getChildren());
                    break;
            }

            // texte
            if (!name.isBlank()) {
                var toWrite = Functions.raccourcir(name, 19);
                Text nameText = new Text(toWrite);
                nameText.setX(40);
                nameText.setY(30);
                nameText.setFill(Color.valueOf("eeeeee"));
                nameText.getStyleClass().add("name-label");
                res.getChildren().add(nameText);
            }

            return res;
        }

        private Group getDebutLineDesign(boolean isParis) {
            Rectangle r1 = new Rectangle();
            r1.setX(10.0);
            r1.setY(0);
            r1.setWidth(12.0f);
            if (isParis) r1.setHeight(40.0f);
            else r1.setHeight(10.0f);
            r1.setFill(Color.valueOf(color));

            Rectangle r2 = new Rectangle();
            r2.setX(10.0f);
            r2.setY(2.0f);
            r2.setWidth(12.0f);
            r2.setHeight(2.0f);
            r2.setFill(Color.valueOf("1f266c"));

            Rectangle r3 = new Rectangle();
            r3.setX(10.0f);
            r3.setY(6.0f);
            r3.setWidth(12.0f);
            r3.setHeight(2.0f);
            r3.setFill(Color.valueOf("1f266c"));

            return new Group(r1, r2, r3);
        }

        private Group getMiddleLineDesign() {
            Rectangle r1 = new Rectangle();
            r1.setX(10.0);
            r1.setY(0);
            r1.setWidth(12.0f);
            r1.setHeight(40.0f);
            r1.setFill(Color.valueOf(color));

            Circle c1 = new Circle();
            c1.setCenterX(16.0f);
            c1.setCenterY(20.0f);
            c1.setRadius(10.0f);
            c1.setFill(Color.valueOf(color));

            Circle c2 = new Circle();
            c2.setCenterX(16.0f);
            c2.setCenterY(20.0f);
            c2.setRadius(7.0f);
            c2.setFill(Color.valueOf("1f266c"));

            return new Group(r1,c1,c2);
        }

        private Group getTerminusLineDesign() {
            Rectangle r1 = new Rectangle();
            r1.setX(10.0f);
            r1.setY(0);
            r1.setWidth(12.0f);
            r1.setHeight(30.0f);
            r1.setFill(Color.valueOf(color));

            Circle c1 = new Circle();
            c1.setCenterX(16.0f);
            c1.setCenterY(20.0f);
            c1.setRadius(13.0f);
            c1.setFill(Color.valueOf(color));

            Circle c2 = new Circle();
            c2.setCenterX(16.0f);
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
