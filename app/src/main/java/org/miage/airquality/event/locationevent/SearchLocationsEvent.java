package org.miage.airquality.event.locationevent;

import org.miage.airquality.model.location.Location;

import java.util.List;

// Event qui permet de renvoyer une liste de location pour les différentes listes de location de l'application
public class SearchLocationsEvent {

    private List<Location> locations;

    public SearchLocationsEvent(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() {
        return locations;
    }


}
