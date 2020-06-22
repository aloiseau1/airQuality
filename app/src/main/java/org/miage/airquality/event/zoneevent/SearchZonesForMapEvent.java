package org.miage.airquality.event.zoneevent;

import org.miage.airquality.model.responsezone.Zone;

import java.util.List;

// Event qui permet de renvoyer une liste de zone pour la carte
public class SearchZonesForMapEvent {

    private List<Zone> resultsZones;

    public SearchZonesForMapEvent(List<Zone> resultsZones) {
        this.resultsZones = resultsZones;
    }

    public List<Zone> getResultsZones() {
        return resultsZones;
    }


}
