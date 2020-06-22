package org.miage.airquality.model.responsemeasurements;

import com.google.gson.annotations.Expose;

import java.util.List;

// Classe qui récupère le tableau des données de mesures de la réponse de l'API Measurement
public class Measurements {
    @Expose
    public List<ResultsMeasurement> measurements;

    public Measurements(List<ResultsMeasurement> resultMeasurements) {
        this.measurements = resultMeasurements;
    }

    public List<ResultsMeasurement> getResultMeasurements() {
        return measurements;
    }

    public void setResultMeasurements(List<ResultsMeasurement> resultMeasurements) {
        this.measurements = resultMeasurements;
    }
}
