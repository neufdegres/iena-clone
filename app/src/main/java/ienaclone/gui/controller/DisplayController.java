package ienaclone.gui.controller;

import java.util.ArrayList;
import java.util.Optional;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.util.Duration;
import ienaclone.gui.Window;
import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.util.DisplayMode;
import ienaclone.gui.view.DisplayView;
import ienaclone.prim.Filter;
import ienaclone.prim.JourneysDataLoader;
import ienaclone.util.AllLinesSingleton;
import ienaclone.util.Files;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayController {
    private final DisplayModel model;
    private DisplayView view;
    private JourneysDataLoader jdl;
    private Service<Void> mainService;
    private Timeline clockTimeline;
    private TranslateTransition disruptionsTransition;
    private String mainServiceStatus;
    private int displayId;

    public DisplayController(DisplayModel m) {
        this.model = m;
        this.mainServiceStatus = "";
        this.displayId = -1;
        mainServiceInit();
        clockTimelineInit();
    }

    /* SETTERS */

    public void setView(DisplayView view) {
        this.view = view;
    }

    public void setJdl(JourneysDataLoader jdl) {
        this.jdl = jdl;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    /* GETTERS */

    public DisplayView getView() {
        return view;
    }

    public Service<Void> getMainService() {
        return mainService;
    }

    public Timeline getClockTimeline() {
        return clockTimeline;
    }

    public Stop getActualStop() {
        return model.getActualStop();
    }

    public int getDisplayId() {
        return displayId;
    }

    /* METHODS */

    public void onViewClosed() {
        clockTimeline.stop();
        mainService.cancel();
        Window.notifyDisplayerClosed(this);
        Functions.writeLog("[" + displayId + "] closed !");
    }

    private void loadTestStop() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        ArrayList<Journey> nextJourneys;

                        // 1 - récupérer les prochains passages

                        nextJourneys = Files.loadTestNextJourneysValues();

                        // 2 - appliquer le filtre (s'il y en a 1)

                        var filtered = applySelectedFilter(nextJourneys);

                        // 3 - récupérer les prochaines gares

                        for (var j : filtered) {                                
                            var data = Files.loadTestNextStopsValues();
                            
                            var dest = j.getDestination().map(s -> s.getCode()).orElse("");

                            var stops = data.getOrDefault(dest, null);

                            if (stops != null) {
                                j.getNextStations().addAll(stops);
                            }
                        }

                        model.getJourneys().clear();
                        model.getJourneys().addAll(filtered);

                        // 4 - on "sélectionne" les journeys à afficher

                        var dm = model.getMode();
                        int limit = 0;

                        if (dm == DisplayMode.ON_PLATFORM_1_TRAIN) limit = 1;
                        else if (dm == DisplayMode.ON_PLATFORM_3_TRAINS) limit = 3;
                        else limit = 5;

                        model.getDisplayedJourneys().addAll(model.getXJourneys(limit));

                        // 5 - on update la vue

                        Platform.runLater(() -> {
                            view.getMain().setTitle(getWindowName());
                            view.updateJourneysView(model.getActualStop(), model.getDisplayedJourneys(), -1);
                            view.updateClock(LocalTime.now()
                                    .format(DateTimeFormatter.ofPattern("HH:mm")));
                            view.getMain().show();
                        });

                        // 6 - on lance le thread qui update l'heure

                        // updateClock();

                        return null;
                    }
                    
                };
            }
            
        }.start();
    }

    public void startLoading() {
        if (model.isTestStop()) {
            loadTestStop();
            return;
        }

        startDisruptionsPanelTransition();
        mainService.start();
    }

    private void mainServiceInit() {
        mainService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        ArrayList<Journey> nextJourneys, allJourneys;

                        int id = -1;

                        while(!isCancelled()) {

                            // 1 - récupérer les prochains passages

                            while (id == jdl.getId()) {
                                TimeUnit.SECONDS.sleep(3);
                            }

                            id++;

                            while (jdl.getMainServiceStatus() == JourneysDataLoader.STATUS.LOADING) {
                                TimeUnit.MILLISECONDS.sleep(300);
                            }

                            switch (jdl.getMainServiceStatus()) {
                                case DATA_SET:
                                    allJourneys = new ArrayList<>();
                                    allJourneys.addAll(jdl.getJourneys());
                                    break;
                                case NO_API_KEY:
                                    mainServiceStatus = "pas de clé d'API.";
                                    this.cancel();
                                    return null;
                                case NO_INTERNET_CONNEXION:
                                    mainServiceStatus = "pas de connexion internet.";
                                    this.cancel();
                                    return null;
                                case ERROR_ELSE:
                                    mainServiceStatus = "autre.";
                                    this.cancel();
                                    return null;
                                default:
                                    allJourneys = new ArrayList<>();
                                    break;
                            }

                            nextJourneys = Filter.removeAlreadyPassedTrains(allJourneys, null);                            

                            // 2 - appliquer le filtre (s'il y en a 1)

                            var filtered = applySelectedFilter(nextJourneys);

                            // 3 - récupérer les prochaines gares (limite 5)

                            TimeUnit.MILLISECONDS.sleep(500);

                            int limit = 0;

                            var dm = model.getMode();

                            if (dm == DisplayMode.ON_PLATFORM_1_TRAIN) limit = 1;
                            else if (dm == DisplayMode.ON_PLATFORM_3_TRAINS) limit = 3;
                            else limit = 5;

                            for (int i = 0; i < filtered.size(); i++) {
                                if (limit == i) break;

                                var tmpj = filtered.get(i);

                                int temp = 25;
                                while (!tmpj.areNextStationsLoaded() && temp > 0) {
                                    // System.out.println(jdl.getSdlServiceStatus()); 
                                    TimeUnit.SECONDS.sleep(1);
                                    temp--;
                                }

                                if (temp == 0) {
                                    mainServiceStatus = "timeout.";
                                    this.cancel();
                                    return null;
                                }
                            }
                            

                            model.getJourneys().clear();
                            model.getJourneys().addAll(filtered);

                            // 4 - on "sélectionne" les journeys à afficher

                            var toDisplay = model.getXJourneys(limit);

                            jdl.updateDisplayedJourneyRefs(id, extractRefs(toDisplay));

                            int diff = getDifference(model.getDisplayedJourneys(), toDisplay);

                            model.getDisplayedJourneys().clear();
                            model.getDisplayedJourneys().addAll(toDisplay);

                            // 5 - on update la vue

                            Platform.runLater(() -> {
                                view.getMain().setTitle(getWindowName());
                                view.updateJourneysView(model.getActualStop(), model.getJourneys(), diff);
                                view.updateClock(LocalTime.now()
                                                    .format(DateTimeFormatter.ofPattern("HH:mm")));
                                view.getMain().show();
                            });

                            // 6 - on lance le thread qui update l'heure (si besoin)

                            Platform.runLater(() -> {
                                if (clockTimeline.getStatus() != Status.RUNNING) {
                                    clockTimeline.play();
                                }
                            });

                            Functions.writeLog("[" + displayId + "] updated !");

                        }

                        return null;
                    }
                    
                };
            }
            
        };

        mainService.stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldValue, Worker.State newValue) {
                switch (newValue) {
                    case FAILED: 
                        mainService.getException().printStackTrace(); 
                    case CANCELLED:
                        if (mainServiceStatus.isEmpty()) break;
                        displayAlert("L'opération de récupération des données a été interrompue : "
                                        + mainServiceStatus);
                        if (mainServiceStatus.equals("timeout."))
                            Window.notifyDisplayerClosed(DisplayController.this);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void clockTimelineInit() {
        clockTimeline = new Timeline(
            new KeyFrame(
                Duration.ZERO, e ->  {
                    Platform.runLater(() -> {
                        var time = LocalDateTime.now();
                        if (time.getSecond() % 2 == 0) {
                            view.updateClock(time.format(DateTimeFormatter.ofPattern("HH:mm")));
                        } else {
                            view.updateClock(time.format(DateTimeFormatter.ofPattern("HH mm")));
                        }
                    });
                }
            ),
            new KeyFrame(Duration.seconds(1))
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
    }

    public void startDisruptionsPanelTransition() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        while (jdl.getStopDLServiceStatus() != JourneysDataLoader.STATUS.DATA_SET) {
                            TimeUnit.MILLISECONDS.sleep(100);
                        }

                        var disr = jdl.getStopDisruptions();
                        model.getDisruptions().setAll(disr);

                        createDisruptionsQueue();

                        if (!model.getDisruptionsQueue().isEmpty()) disruptionsTransitionPlay();

                        return null;
                    }
                    
                };
            }
            
        }.start();
    }

    public void disruptionsPanelTransitionInit() {
        var panel = view.getDisruptionsBox();
        
        disruptionsTransition = new TranslateTransition(Duration.seconds(1), panel);
        disruptionsTransition.setFromX(-400);
        disruptionsTransition.setToX(0);
        disruptionsTransition.setInterpolator(Interpolator.EASE_OUT); 
        disruptionsTransition.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(9));
            pause.setOnFinished(e2 -> {
                TranslateTransition exitTransition = new TranslateTransition(Duration.seconds(1), panel);
                exitTransition.setFromX(0);
                exitTransition.setToX(-400);
                exitTransition.setInterpolator(Interpolator.EASE_IN);
                exitTransition.setOnFinished(e3 -> {
                    disruptionsTransitionPlay();
                });
                exitTransition.play();
            });
            pause.play();
        });
    }

    private void disruptionsTransitionPlay() {
        // 1 - pop une annonce
        var next = model.getDisruptionsQueue().dequeue(); 
        // 2 - l'afficher dans le carré
        view.updateDisruptionView(next.getType(), next.getMessage());

        disruptionsTransition.play();
    }

    public String getWaitingTimeLabel(Journey journey, Stop actual) {
        if (model.isTestStop()) return "-- min";

        var status = journey.getStopStatus(actual);

        LocalDateTime arrival = null;
        LocalDateTime departure = null;

        if (status == Stop.STATUS.TERMINUS) return "-- min";

        if (journey.getExpectedArrivalTime().isPresent()) {
            arrival = journey.getExpectedArrivalTime().get();
        } else if (journey.getAimedArrivalTime().isPresent()) {
            arrival = journey.getAimedArrivalTime().get();
        }

        if (journey.getExpectedDepartureTime().isPresent()) {
            departure = journey.getExpectedDepartureTime().get();
        } else if (journey.getAimedDepartureTime().isPresent()) {
            departure = journey.getAimedDepartureTime().get();
        }

        if (arrival != null || departure != null) {
            long timeUtilArrival = Functions.getWaitingTime(arrival);
            long timeUtilDeparture = Functions.getWaitingTime(departure);

            if (timeUtilArrival >= 3600 || timeUtilDeparture >= 3600) {
                if (timeUtilArrival >= 3600)
                    return arrival.format(DateTimeFormatter.ofPattern("HH:mm"));
                return departure.format(DateTimeFormatter.ofPattern("HH:mm"));
            }

            if (timeUtilArrival > 120 || timeUtilDeparture > 120) {
                if (timeUtilArrival > 120) return timeUtilArrival/60 + " min";
                return timeUtilDeparture/60 + " min";
            }

            if (status == Stop.STATUS.START) {
                if (arrival != null) {
                    if (timeUtilArrival > 0) return "à l'approche";
                }
                return "imminent";
            }

            if (arrival != null) {
                if (timeUtilArrival > 60) return timeUtilArrival/60 + " min";
                if (timeUtilArrival > 0) return "à l'approche";
                return "à quai";
            }

            // normalement on n'arrive jamais dans ce cas de figure...
            if (timeUtilDeparture > 1) return "à l'approche";
            return "à quai";
        }

        return "-- min";        
    }

    private void displayAlert(String text) {
        var alert = new Alert(AlertType.ERROR);
        alert.setContentText(text);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            view.getMain().close();
        }

    }

    private String getWindowName() {
        String res = "";
        res += model.getActualStop().getName();
        res += " ";
        if (model.getFilter().key() != null) {
            res += "(" + model.getFilter().key();
            res += " : " + model.getFilter().value();
            res += ") ";
        } else {
            res += "(non filtré) ";
        }
        switch(model.getMode()) {
            case ON_PLATFORM_1_TRAIN:
                res += "[1 train]";
                break;
            case ON_PLATFORM_3_TRAINS:
                res += "[3 train]";
                break;
            case OUT_OF_PLATFORM:
                res += "[5 train]";
                break;            
        }

        return res;
    }

    // TODO : pour l'instant on admet que chaque train part chacun à la suite des autres
    private int getDifference(ArrayList<Journey> older, ArrayList<Journey> newer) {
        if (older.isEmpty() && newer.isEmpty()) return 0;
        if (newer.isEmpty()) return -older.size(); // TODO : gérer cas ou plus de trains
        if (older.isEmpty()) return -newer.size();

        int idx = older.indexOf(newer.get(0));

        if (idx == -1) return -older.size();

        return -idx;
    }

    private ArrayList<Journey> applySelectedFilter(ArrayList<Journey> raw) {
        var filter = model.getFilter();

        String key = filter.key();
        String value = filter.value();

        if (key == null) return raw;

        switch (key) {
            case "direction":
                return (ArrayList<Journey>) Filter.byDirection(raw, value);
            case "platform":
                return (ArrayList<Journey>) Filter.byPlatform(raw, value);
            case "mission":
                return (ArrayList<Journey>) Filter.byMission(raw, value);
            case "line":
                var ligne = AllLinesSingleton.getInstance().getLineByName(value);
                String code = "";
                if (ligne.isPresent()) code = ligne.get().getCode();
                return (ArrayList<Journey>) Filter.byLine(raw, code);
        }

        return raw;
    }

    private String[] extractRefs(ArrayList<Journey> l) {
        int len = l.size();
        String[] res = new String[len];
        for(int i=0; i<len; i++) {
            res[i] = l.get(i).getRef();
        }
        return res;
    }

    private void createDisruptionsQueue() {
        var queue = model.getDisruptionsQueue();
        var d = model.getDisruptions();

        var informations = d.getInformations();
        var perturbations = d.getPerturbations();
        var commercial = d.getCommercial();

        int cI=0, cP=0, cC=0;
        boolean tI = false, tP=false, tC=false;

        while(!(tI && tP && tC)) {
            if (!informations.isEmpty()) {
                queue.enqueue(informations.get(cI));
                cI = (cI+1) % informations.size();
                if (cI == 0) tI = true;
            } else tI = true;

            if (!perturbations.isEmpty()) {
                queue.enqueue(perturbations.get(cP));
                cP = (cP+1) % perturbations.size();
                if (cP == 0) tP = true;
            } else tP = true;

            if (!commercial.isEmpty()) {
                queue.enqueue(commercial.get(cC));
                cC = (cC+1) % commercial.size();
                if (cC == 0) tC = true;
            } else tC = true;
        }
    }
}