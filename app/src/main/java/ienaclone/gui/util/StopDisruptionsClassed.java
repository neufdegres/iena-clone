package ienaclone.gui.util;

import java.util.List;
import java.util.ArrayList;

import ienaclone.util.StopDisruption;

public class StopDisruptionsClassed {
    private final ArrayList<StopDisruption> informations;
    private final ArrayList<StopDisruption> perturbations;
    private final ArrayList<StopDisruption> commercial;

    public StopDisruptionsClassed() {
        this.informations = new ArrayList<>();
        this.perturbations = new ArrayList<>();
        this.commercial = new ArrayList<>();
    }

    public ArrayList<StopDisruption> getInformations() {
        return informations;
    }

    public ArrayList<StopDisruption> getPerturbations() {
        return perturbations;
    }

    public ArrayList<StopDisruption> getCommercial() {
        return commercial;
    }

    public void setAll(List<StopDisruption> l) {
        sort(l);
    }

    private void sort(List<StopDisruption> l) {
        for (var disr : l) {
            switch (disr.getType()) {
                case INFORMATION: informations.add(disr); break;
                case PERTURBATION: perturbations.add(disr); break;
                case COMMERCIAL: commercial.add(disr); break;
            }
        }
    }
}
