package org.miage.airquality.model.responsemeasurements;

import com.google.gson.annotations.Expose;

// Classe qui récupère les données de la réponse de l'API Measurement
public class ResultsMeasurement {

    @Expose
    public String parameter; //Type de la donnée de la mesure (ex:pm10)
    @Expose
    public Double value; // Valeur de la mesure
    @Expose
    public String unit; // Unité de la valeur

    public ResultsMeasurement(String parameter, Double value, String unit) {
        this.parameter = parameter;
        this.value = value;
        this.unit = unit;
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
