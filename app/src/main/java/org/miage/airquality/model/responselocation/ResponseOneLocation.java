package org.miage.airquality.model.responselocation;

import com.google.gson.annotations.Expose;

// Classe qui récupère la réponse de l'API Location mais qui retourne seulement une location (recherche par idLocation)
public class ResponseOneLocation {
    @Expose
    public ResultsLocation results;

    public ResponseOneLocation(ResultsLocation results) {
        this.results = results;
    }

    public ResultsLocation getResults() {
        return results;
    }

    public void setResults(ResultsLocation results) {
        this.results = results;
    }
}
