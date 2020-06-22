package org.miage.airquality.event.locationevent;

import org.miage.airquality.model.location.Location;

import java.util.List;

// Event qui permet de renvoyer une liste de location pour la carte
public class SearchLocationsForMapEvent {

    private List<Location> locations;

    public SearchLocationsForMapEvent(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() {
        return locations;
    }


}
