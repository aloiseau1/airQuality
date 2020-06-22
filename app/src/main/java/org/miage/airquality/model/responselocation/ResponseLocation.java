package org.miage.airquality.model.responselocation;

import java.util.List;
import com.google.gson.annotations.Expose;

// Classe qui récupère la réponse de l'API Location
public class ResponseLocation {
    @Expose
    public List<ResultsLocation> results;

    public ResponseLocation(List<ResultsLocation> results) {
        this.results = results;
    }

    public List<ResultsLocation> getResults() {
        return results;
    }

    public void setResults(List<ResultsLocation> results) {
        this.results = results;
    }
}
