package ienaclone.gui.controller;

import java.util.ArrayList;

import ienaclone.gui.model.DisplayModel;
import ienaclone.gui.view.DashboardView.FilterBox;
import ienaclone.gui.view.OnPlatformDisplayView;
import ienaclone.prim.Parcer;
import ienaclone.prim.Requests;
import ienaclone.util.Files;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class DisplayController {
    private DisplayModel model;
    private OnPlatformDisplayView view;

    public DisplayController(DisplayModel m) {
        this.model = m;
    }

    public void setView(OnPlatformDisplayView view) {
        this.view = view;
    }

	public void firstLoad() {
        /* TODO :
         * On a :
         * - les données de l'arrêt demandée
         * - le filtre appliqué
         * - si c'est le "test stop" ou non
         * 
         * A partir de tt ça :
         * - Faire une requête à l'API 1 pour obtenir les prochains passages
         * - Pour chaque passage **affiché**, faire une requête à l'API 2 pour
         * obtenir les horraires + les prochains arrêts de chaque train
         * - Donner l'heure actuelle
         */

        if (model.isTestStop()) {
            
        } else {
            new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {
                            // var rep = Requests.getNextJourneys(model.getCurrentStop().getCode());
                            // if (rep.containsKey("data")) {
                            //     nextJourneys = rep.get("data");
                            // } else {
                            //     return null;
                            // }
                            return null;
                        }
                        
                    };
                }
                
            }.start();
        }

        /* if (!model.isOnlyNext()) {
            // TODO : à implémenter
            // à  récupérer :
            // - pictogramme
            // - destination
            // - code mission
            // - temps d'attente
            // - liste des gares
        } else {
            
        }
        view.getMain().show(); */
	}

    public void loadFirstJourney() {
        new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected Void call() throws Exception {
                        /* ArrayList<Journey> nextJourneys;
                        
                        var rep = Requests.getNextJourneys(model.getCurrentStop().getCode());
                            if (rep.containsKey("data")) {
                                nextJourneys = rep.get("data");
                            } else {
                                Platform.runLater(() -> {
                                    if (rep.containsKey("error_internet")) {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.NO_INTERNET_CONNEXION, null);
                                    } else if (rep.containsKey("error_apikey")) {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.NO_API_KEY, null);
                                    } else {
                                        view.getFilterBox().changeStatus(
                                            FilterBox.STATUS.ERROR, null);
                                    }
                                });
                                return null;
                            }
                        }

                        model.setJourneys(nextJourneys); */
                        
                        return null;
                    }
                    
                };
                
            } 
        }.start();
    }
    
}