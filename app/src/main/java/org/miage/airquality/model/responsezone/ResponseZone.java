package org.miage.airquality.model.responsezone;

import com.google.gson.annotations.Expose;

import java.util.List;

// Classe qui récupère la réponse de l'API Zone
public class ResponseZone {
    @Expose
    public List<Zone> results;

    public ResponseZone(List<Zone> results) {
        this.results = results;
    }

    public List<Zone> getResults() {
        return results;
    }

    public void setResults(List<Zone> results) {
        this.results = results;
    }
}
