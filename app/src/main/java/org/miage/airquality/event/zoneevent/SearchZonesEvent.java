package org.miage.airquality.event.zoneevent;

import org.miage.airquality.model.responsezone.Zone;

import java.util.List;

// Event qui permet de renvoyer une liste de zone pour les différentes listes de zone de l'application
public class SearchZonesEvent {

    private List<Zone> resultsZones;

    public SearchZonesEvent(List<Zone> resultsZones) {
        this.resultsZones = resultsZones;
    }

    public List<Zone> getResultsZones() {
        return resultsZones;
    }


}
