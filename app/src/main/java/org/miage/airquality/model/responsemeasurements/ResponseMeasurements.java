package org.miage.airquality.model.responsemeasurements;

import com.google.gson.annotations.Expose;

import java.util.List;

// Classe qui récupère la réponse de l'API Measurement
public class ResponseMeasurements {
    @Expose
    public List<Measurements> results;

    public ResponseMeasurements(List<Measurements> results) {
        this.results = results;
    }

    public List<Measurements> getMeasurements() {
        return results;
    }

    public void setMeasurements(List<Measurements> results) {
        this.results = results;
    }
}
