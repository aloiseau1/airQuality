package org.miage.airquality.model.responselocation;

import com.google.gson.annotations.Expose;

import java.util.List;

// Classe qui récupère les données d'une location à partir de la réponse de l'API Location
public class ResultsLocation {

    @Expose
    public String id;
    @Expose
    public String city;
    @Expose
    public List<String> cities;
    @Expose
    public String location;
    @Expose
    public CoordinatesGson coordinates;
    @Expose
    public List<String> parameters;

    public ResultsLocation(String id, String city, List<String> cities, String location, CoordinatesGson coordinates, List<String> parameters) {
        this.id = id;
        this.city = city;
        this.cities = cities;
        this.location = location;
        this.coordinates = coordinates;
        this.parameters = parameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getCities() {
        return cities;
    }

    public void setCities(List<String> cities) {
        this.cities = cities;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public CoordinatesGson getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(CoordinatesGson coordinates) {
        this.coordinates = coordinates;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}
