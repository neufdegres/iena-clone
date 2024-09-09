package ienaclone.prim;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.util.Pair;
import javafx.concurrent.Service;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

import ienaclone.util.Stop;
import ienaclone.util.TimeStatus;
import ienaclone.prim.Requests.StopData;
import ienaclone.util.AllStopsSingleton;
import ienaclone.util.Functions;
import ienaclone.util.Journey;

public class JourneysDataLoader {
    private final Stop stop;
    private Service<Void> mainService, stopsDataLoadService;
    private volatile ArrayList<Journey> journeys;
    private volatile STATUS mainServiceStatus, sdlServiceStatus;
    private volatile int id;

    public enum STATUS {NOT_STARTED, LOADING, DATA_SET, NO_INTERNET_CONNEXION, NO_API_KEY, ERROR_ELSE}

    public JourneysDataLoader(Stop stop) {
        assert(stop != null);
        this.stop = stop;
        journeys = new ArrayList<>();
        mainServiceStatus = STATUS.NOT_STARTED;
        sdlServiceStatus = STATUS.NOT_STARTED;
        id = -1;
        mainServiceInit();
        stopsDataLoadServiceInit();
    }

    public List<Journey> getJourneys() {
        var obs = FXCollections.observableArrayList(journeys);
        return FXCollections.unmodifiableObservableList(obs);
    }

    public Service<Void> getMainService() {
        return mainService;
    }

    public STATUS getMainServiceStatus() {
        return mainServiceStatus;
    }

    public STATUS getSdlServiceStatus() {
        return sdlServiceStatus;
    }

    public int getId() {
        return id;
    }

    private void mainServiceInit() {
        mainService =  new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        // update toutes les 40 secondes les horraires des trains
                        while(!isCancelled()) {
                            switch (sdlServiceStatus) {
                                case NO_API_KEY:
                                case NO_INTERNET_CONNEXION:
                                case ERROR_ELSE:
                                    // Functions.writeLog("sdl erreur !!");
                                    this.cancel(); 
                                    break;
                                default:
                                    break;                               
                            }

                            id++;
                            mainServiceStatus = STATUS.LOADING;
                            Functions.writeLog("journeys data loading !");

                            var rep = Requests.getNextJourneys(stop.getCode());
                            
                            if (rep.containsKey("data")) {
                                updateJourneys(rep.get("data"));
                                mainServiceStatus = STATUS.DATA_SET;
                            } else {
                                if (rep.containsKey("error_internet")) {
                                    mainServiceStatus = STATUS.NO_INTERNET_CONNEXION;
                                } else if (rep.containsKey("error_apikey")) {
                                    mainServiceStatus = STATUS.NO_API_KEY;
                                } else {
                                    mainServiceStatus = STATUS.ERROR_ELSE;
                                }                
                            }

                            switch (mainServiceStatus) {
                                case DATA_SET:
                                    Functions.writeLog("journeys data loaded !");
                                    Platform.runLater(() -> {
                                        if (stopsDataLoadService.isRunning()) {
                                            stopsDataLoadService.restart();
                                        } else {
                                            stopsDataLoadService.start();
                                        }
                                    });
                                    break;
                                case NO_API_KEY:
                                case NO_INTERNET_CONNEXION:
                                case ERROR_ELSE:
                                    // Functions.writeLog("main erreur !!");
                                    this.cancel(); 
                                    break;
                                default:
                                    break;                               
                            }

                            try {
                                TimeUnit.SECONDS.sleep(40);
                            } catch(InterruptedException e) {
                                this.cancel();
                            }
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
                        System.out.println(mainService.getException().getMessage()); 
                        if (mainServiceStatus != STATUS.NO_API_KEY
                                && mainServiceStatus != STATUS.NO_INTERNET_CONNEXION) {
                            mainServiceStatus = STATUS.ERROR_ELSE;
                        }
                        break;
                    case CANCELLED:
                        if (mainServiceStatus != STATUS.NO_API_KEY
                                && mainServiceStatus != STATUS.NO_INTERNET_CONNEXION) {
                            mainServiceStatus = STATUS.ERROR_ELSE;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void stopsDataLoadServiceInit() {
        stopsDataLoadService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        Functions.writeLog("stops data loading !");
                        sdlServiceStatus = STATUS.LOADING;

                        var sorted = sortMissions(journeys);
                        var missions = sorted.missions();
                        var rest = sorted.rest();

                        for (var m : missions) {
                            var tmp = journeys.stream()
                                    .filter(j -> j.getMission().orElse(null) != null
                                                && j.getMission().get().equals(m.getKey()))
                                    .toList();

                            var toFill = tmp.stream()
                                    .filter(j -> !j.areNextStationsLoaded())
                                    .toList();

                            if (!toFill.isEmpty()) {
                                ArrayList<StopData> stopsData;

                                var rep = Requests.getJourneyStopList(m.getValue());

                                if (rep.containsKey("data")) {
                                    stopsData = rep.get("data");
                                    var tmp2 = parseStopsData(stopsData);

                                    for (var tf : toFill) {
                                        tf.getNextStations().addAll(tmp2);
                                        tf.setNextStationsLoaded(true);
                                    }
                                } else {
                                    if (rep.containsKey("error_internet")) {
                                        sdlServiceStatus = STATUS.NO_INTERNET_CONNEXION;
                                    } else if (rep.containsKey("error_apikey")) {
                                        sdlServiceStatus = STATUS.NO_API_KEY;
                                    } else if (rep.containsKey("unknown_ref")) {
                                        // System.err.println("ref inconnue : " + m.getValue());
                                        // System.err.println("nom mission : " + m.getKey());
                                        for (var tf : toFill) {
                                            tf.setNextStationsLoaded(true);
                                        }
                                        // Functions.writeLog("done !");
                                    } else {
                                        sdlServiceStatus = STATUS.ERROR_ELSE;
                                    }
                                    return null;
                                }
                                
                            }
                        }

                        for (var r : rest) {
                            ArrayList<StopData> stopsData;

                            if (r.areNextStationsLoaded()) continue;

                            var rep = Requests.getJourneyStopList(r.getRef());

                            if (rep.containsKey("data")) {
                                stopsData = rep.get("data");
                                var tmp2 = parseStopsData(stopsData);

                                r.getNextStations().addAll(tmp2);
                                r.setNextStationsLoaded(true);
                            } else {
                                if (rep.containsKey("error_internet")) {
                                    sdlServiceStatus = STATUS.NO_INTERNET_CONNEXION;
                                } else if (rep.containsKey("error_apikey")) {
                                    sdlServiceStatus = STATUS.NO_API_KEY;
                                } else if (rep.containsKey("unknown_ref")) {
                                    // System.err.println("ref inconnue : " + r.getRef());
                                    // System.err.println("heure d'arrivée : " + r.getExpectedArrivalTime());
                                    // System.err.println("heure de depart : " + r.getExpectedDepartureTime());
                                    r.setNextStationsLoaded(true);
                                } else {
                                    sdlServiceStatus = STATUS.ERROR_ELSE;
                                }
                                return null;
                            }
                        }

                        Functions.writeLog("stops data loaded !");

                        return null;
                    }
                };
            }
        };

        stopsDataLoadService.stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldValue, Worker.State newValue) {
                switch (newValue) {
                    case FAILED:
                    case CANCELLED:
                        mainServiceStop();
                        break;
                    case SUCCEEDED:
                        stopsDataLoadService.reset();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void mainServiceStart() {
        mainService.start();
    }

    public void mainServicePause() {
        mainService.cancel();
        mainService.reset();
    }

    public void mainServiceStop() {
        mainService.cancel();
    }

    public void updateJourneys(ArrayList<Journey> newest) {
        // si newest vide, on vide journeys 
        if (newest.isEmpty()) {
            if (!journeys.isEmpty()) journeys.clear();
            return;
        }

        // si journeys vide mais non newest
        if (journeys.isEmpty()) {
            journeys.addAll(newest);
            return;
        }

        // autres cas

        // on enlève les voyages trop vieux
        for (int i=0; i<journeys.size(); i++) {
            var tmp = journeys.get(i);
            if (!newest.contains(tmp)) {
                journeys.remove(i);
                i--;
            } else {
                break;
            }
        }

        // on ajoute les nouveaux
        var last = journeys.get(journeys.size()-1);
        int idx = newest.indexOf(last);
        if (idx+1 < newest.size())
            journeys.addAll(newest.subList(idx+1, newest.size()));
    }

    public void clearJourneys() {
        journeys.clear();
    }

    private ArrayList<Pair<Stop, Stop.STATUS>> parseStopsData(ArrayList<StopData> sd) {
        var res = new ArrayList<Pair<Stop, Stop.STATUS>>();
        
        for (var st : sd) {
            var stop = AllStopsSingleton.getInstance()
                            .getStopByCode(st.stopRef()).orElse(new Stop());
            
            Pair<Stop, Stop.STATUS> pair = null;

            if (st.skippedStop()) {
                pair = new Pair<>(stop, Stop.STATUS.SKIPPED);
            } else if (st.pickupAllowed() && !st.dropOffAllowed()) {
                pair = new Pair<>(stop, Stop.STATUS.START);
            } else if (!st.pickupAllowed() && st.dropOffAllowed()) {
                pair = new Pair<>(stop, Stop.STATUS.TERMINUS);
            } else if (!st.skippedStop()) {
                pair = new Pair<>(stop, Stop.STATUS.INCLUDED);
            } else {
                pair = new Pair<>(stop, Stop.STATUS.UNKOWN);
            }

            res.add(pair);
        }

        return res;
    }

    private MissionsSorted sortMissions(List<Journey> journeys) {
        var missions = new ArrayList<Pair<String, String>>();
        var rest = new ArrayList<Journey>();

        journeys.stream()
                .forEach(j -> {
                    if (j.getTimeStatus() == TimeStatus.CANCELLED) {
                        j.setNextStationsLoaded(true);
                    } else {
                        var mission = j.getMission().orElse(null);
                        var ref = j.getRef();
                        if (mission == null) rest.add(j);
                        else if (missions.stream().allMatch(m -> !m.getKey().equals(mission))) {
                            missions.add(new Pair<String,String>(mission, ref));
                        }
                    }
                });

        return new MissionsSorted(missions, rest);
    }

    private record MissionsSorted(ArrayList<Pair<String, String>> missions, ArrayList<Journey> rest) {}

}
