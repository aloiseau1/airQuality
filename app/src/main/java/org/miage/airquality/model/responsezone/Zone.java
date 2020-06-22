package org.miage.airquality.model.responsezone;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

// Classe qui récupère les données de la réponse de l'API Zone
@Table(name = "Zone")
public class Zone extends Model {

    // Clé unique name, c'est à partir de cette clé que les données d'une zone sont retrouvées
    @Expose
    @Column(name = "name", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String name;

    @Column(name = "longitude")
    public Double longitude;

    @Column(name = "latitude")
    public Double latitude;

    public Zone() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
