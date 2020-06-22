package org.miage.airquality.model.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

// Classe qui contient les coordonnées d'une location
@Table(name = "Coordinates")
public class Coordinates extends Model implements Serializable {

    // La clé unique idLocation permet de récupéré les corrdonnées d'une location en BDD
    @Column(name = "idLocation", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String idLocation;

    @Column(name = "longitude")
    public Double longitude;

    @Column(name = "latitude")
    public Double latitude;

    public Coordinates() {
        super();
    }

    public String getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(String idLocation) {
        this.idLocation = idLocation;
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
