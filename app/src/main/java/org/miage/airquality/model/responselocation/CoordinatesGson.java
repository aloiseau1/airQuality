package org.miage.airquality.model.responselocation;

import com.google.gson.annotations.Expose;

// Classe qui récupère les coordonnées d'une location à partir de la réponse de l'API Location
public class CoordinatesGson{

    @Expose
    public Double longitude;

    @Expose
    public Double latitude;

    public CoordinatesGson(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
