package org.miage.airquality.model.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

// Classe qui regroupe le nom d'un paramètre géré par une location
@Table(name = "Parameter")
public class Parameter extends Model implements Serializable {

    // La clé unique est composé de deux attributs idLocation et parameter cela permet l'unicité des données pour un paramètre donné et une location donnée
    // L'attribut idLocation permet de récupérer les paramètres gérés d'une location
    @Column(name = "idLocation", index = true, uniqueGroups = {"groupParameter"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String idLocation;

    @Column(name = "parameter", uniqueGroups = {"groupParameter"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String parameter;

    public Parameter() {
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
}
