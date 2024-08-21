package ienaclone.gui.controller;

import java.util.ArrayList;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.view.DisplayView;
import ienaclone.prim.Filter;
import ienaclone.prim.Requests;
import ienaclone.util.AllLinesSingleton;
import ienaclone.util.AllStopsSingleton;
import ienaclone.util.Files;
import ienaclone.util.Functions;
import ienaclone.util.Journey;
import ienaclone.util.Stop;

public class DisplayController {
    private DisplayModel model;
    private DisplayView view;

    public DisplayController(DisplayModel m) {
        this.model = m;
    }

    public void setView(DisplayView view) {
        this.view = view;
    }

	public void firstLoad() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        ArrayList<Journey> nextJourneys;

                        // 1 - récupérer les prochains passages

                        if (model.isTestStop()) {
                            nextJourneys = Files.loadTestNextJourneysValues();
                        } else {
                            ArrayList<Journey> allJourneys;
                            var rep = Requests.getNextJourneys(model.getActualStop().getCode());
                            if (rep.containsKey("data")) {
                                allJourneys = rep.get("data");
                            } else {
                                if (rep.containsKey("error_internet")) {
                                    System.err.println("req1 : pas de connexion internet");
                                } else if (rep.containsKey("error_apikey")) {
                                    System.err.println("req1 : pas de clée api");
                                } else {
                                    System.err.println("req1 : autre erreur");
                                }
                                return null;
                            }

                            nextJourneys = Filter.removeAlreadyPassedTrains(allJourneys, null);
                        }

                        // 2 - appliquer le filtre (s'il y en a 1)

                        var filtered = applySelectedFilter(nextJourneys);

                        // 3 - récupérer les prochaines gares (limite 6)

                        if (model.isTestStop()) {
                            for (var j : filtered) {                                
                                var data = Files.loadTestNextStopsValues();
                                
                                var dest = j.getDestination().map(s -> s.getCode()).orElse("");

                                var stops = data.getOrDefault(dest, null);

                                if (stops != null) j.getNextStations().addAll(stops);
                            }

                        } else {
                            // TODO : parraléliser l'étape ?

                            int i=0;
                            for (var j : filtered) {
                                ArrayList<String> stopRefs;

                                var ref = j.getRef();
                                var rep2 = Requests.getJourneyStopList(ref);

                                if (rep2.containsKey("data")) {
                                    stopRefs = rep2.get("data");
                                    ArrayList<Stop> tmp = new ArrayList<>();
                                    for (var sr : stopRefs) {
                                        var stop = AllStopsSingleton.getInstance().getStopByCode(sr);
                                        tmp.add(stop.orElse(new Stop()));
                                    }
                                    var tmp2 = removeAlreadyPassedStops(tmp);
                                    j.getNextStations().addAll(tmp2);
                                } else {
                                    // TODO
                                    if (rep2.containsKey("error_internet")) {
                                        System.err.println("req2 : pas de connexion internet");
                                    } else if (rep2.containsKey("error_apikey")) {
                                        System.err.println("req2 : pas de clée api");
                                    } else {
                                        System.err.println("req2 : autre erreur");
                                    }
                                    return null;
                                }

                                i++;
                                if (i>5) break;
                            }
                        }

                        model.getJourneys().clear();
                        model.getJourneys().addAll(filtered);

                        // 4 - on update la vue

                        Platform.runLater(() -> {
                            view.getMain().setTitle(getWindowName());
                            view.updateView(model.getJourneys());
                            view.updateClock(LocalTime.now());
                            view.getMain().show();
                        });

                        // 5 - on lance le thread qui update l'heure

                        updateClock();

                        return null;
                    }
                    
                };
            }
            
        }.start();
	}

    public void updateClock() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        LocalTime now = LocalTime.now();
                        int min = now.getMinute();
                        while (true) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                                now = LocalTime.now();
                                if (now.getMinute() != min) {
                                    final var toUpdate = LocalTime.of(now.getHour(), now.getMinute());
                                    min = now.getMinute();
                                    Platform.runLater(() -> {
                                        view.updateClock(toUpdate);
                                    });
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        } 
                    }
                };
            }
            
        }.start();
    }

    private String getWaitingTimeLabel(Journey journey) {
        LocalDateTime toWait = null;
        if (journey.getExpectedArrivalTime().isPresent()) {
            toWait = journey.getExpectedArrivalTime().get();
        } else if (journey.getExpectedDepartureTime().isPresent()) {
            toWait = journey.getExpectedDepartureTime().get();
        } else if (journey.getExpectedArrivalTime().isPresent()) {
            toWait = journey.getExpectedArrivalTime().get();
        } else if (journey.getExpectedDepartureTime().isPresent()) {
            toWait = journey.getExpectedDepartureTime().get();
        }

        if (toWait != null) {
            long time = Functions.getWaitingTime(toWait);
            return time + " min";
        }

        return "-- min";        
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

    private ArrayList<Stop> removeAlreadyPassedStops(ArrayList<Stop> stops) {
        int idx = stops.indexOf(model.getActualStop());
        var tmp = stops.subList(idx+1, stops.size());
        return new ArrayList<Stop>(tmp);
    }
    
}