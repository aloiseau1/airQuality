package org.miage.airquality.model.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

// Classe qui regroupe la mesure d'un paramètre pour une location
@Table(name = "LocationMeasurement")
public class LocationMeasurement extends Model implements Serializable {

    // La clé unique est composé de deux attributs idLocation et parameter cela permet l'unicité des données pour un paramètre donné et une location donnée
    // L'attribut idLocation permet de récupérer les mesures de tous les paramètres d'une location
    @Column(name = "idLocation", index = true, uniqueGroups = {"groupLocationMeasurement"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String idLocation;

    @Column(name = "parameter", index = true, uniqueGroups = {"groupLocationMeasurement"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String parameter;

    @Column(name = "value")
    public Double value;

    @Column(name = "unit")
    public String unit;

    public LocationMeasurement() {
        super();
    }

    public String getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(String idLocation) {
        this.idLocation = idLocation;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Double getValue() {
        return value;
    }

    // Récupération de la valeur du paramètre au format String
    // Retourne "/" si la valeur du paramètre est null
    public String getStringValue() {
        if(value != null){
            return value.toString();
        } else {
            return "/";
        }
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
