package org.miage.airquality.model.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.io.Serializable;
import java.util.List;

// Classe qui contient toutes les infos d'une location
@Table(name = "Location")
public class Location extends Model implements Serializable {

    // Clé unique idLocation, c'est à partir de cette clé que les données de la location sont retrouvées
    @Column(name = "idLocation", index = true, unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String idLocation;
    @Column(name = "city")
    public String city;
    @Column(name = "location")
    public String location;
    @Column(name = "favoris")
    public Boolean favoris;
    @Column(name = "nameZone")
    public String nameZone;

    public Coordinates coordinates;
    public List<Parameter> parameters;
    public List<LocationMeasurement> measurements;
    public List<DayPrevision> dayPrevisions;

    public Location() {
        super();
    }

    public String getNameZone() {
        return nameZone;
    }

    public void setNameZone(String nameZone) {
        this.nameZone = nameZone;
    }

    public String getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(String idLocation) {
        this.idLocation = idLocation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Coordinates getCoordinates() {
        // Récupération des coordonnées de la location grâce à son idLocation
        coordinates = new Select().
                from(Coordinates.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .executeSingle();
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Parameter> getParameters() {
        // Récupération des parametres de la location grâce à son idLocation
        parameters = new Select().
                from(Parameter.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .execute();
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<LocationMeasurement> getMeasurements() {
        // Récupération des mesures de la location grâce à son idLocation
        measurements = new Select().
                from(LocationMeasurement.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .execute();
        return measurements;
    }

    public void setMeasurements(List<LocationMeasurement> measurements) {
        this.measurements = measurements;
    }

    public List<DayPrevision> getDayPrevisions() {
        // Récupération des jours de prévision météo de la location grâce à son idLocation
        dayPrevisions = new Select().
                from(DayPrevision.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .orderBy("date ASC")
                .execute();
        return dayPrevisions;
    }

    public List<DayPrevision> getTodayDayPrevisions(String todayDate) {
        // Récupération des jours de prévision météo de la location à partir de la date du jour, grâce à son idLocation
        dayPrevisions = new Select().
                from(DayPrevision.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .and("dateDebut LIKE \"" + todayDate + "\"")
                .orderBy("date ASC")
                .execute();
        return dayPrevisions;
    }

    public void setDayPrevisions(List<DayPrevision> dayPrevisions) {
        this.dayPrevisions = dayPrevisions;
    }

    public Boolean getFavoris() {
        return favoris;
    }

    public void setFavoris(Boolean favoris) {
        this.favoris = favoris;
    }

    // Récupération d'une chaîne de caractère au format : "<NOM_PARAMETRE>: <VALEUR_MESURE> <UNITE_MESURE>",
    // en fonction du paramètre reçu en paramètre de la méthode
    // Méthode utilisée pour l'affichage de données des mesures dans la liste des locations et les détails d'une location
    public String getOneMeasurement(String parameter){
        String result = parameter + ": /";
        for (LocationMeasurement measurement: this.getMeasurements()) {
            if (measurement.getParameter().equals(parameter)){
                result = measurement.getParameter() + ": " + measurement.getStringValue() + " " + measurement.getUnit() + "   ";
            }
        }
        return result;
    }

    // Récupération de la valeur d'une mesure dont le nom du paramètre est reçu en paramètre de la méthode
    public Double getValueOfOneMeasurement(String parameter){
        Double result = null;
        for (LocationMeasurement measurement: this.getMeasurements()) {
            if (measurement.getParameter().equals(parameter)){
                result = measurement.getValue();
            }
        }
        return result;
    }

    // Récupération de la temperature actuelle grâce aux jours de prévision de la location
    public String getTempActuelle(){
        Boolean find = false;
        Integer compt = 0;
        String temperature = "";
        while(!find && compt < this.getDayPrevisions().size()){
            if(this.getDayPrevisions().get(compt).getTempActuelle() != null){
                temperature = this.getDayPrevisions().get(compt).getStringTempActuelle();
                find = true;
            } else {
                compt++;
            }
        }
        return temperature;
    }

}
