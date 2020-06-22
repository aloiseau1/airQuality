package org.miage.airquality.model.location;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// Classe qui regroupe toutes les données météos d'un jour, les données sont divisées en deux parties : matin et après-midi
@Table(name = "DayPrevision")
public class DayPrevision extends Model implements Serializable {

    // La clé unique est composé de deux attributs idLocation et date cela permet l'unicité des données pour une date donnée et une location donnée
    // L'attribut idLocation permet de récupérer les prévisions météo pour une location
    @Column(name = "idLocation", index = true, uniqueGroups = {"groupDayPrevision"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String idLocation;

    @Column(name = "date", uniqueGroups = {"groupDayPrevision"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String date;

    @Column(name = "dateDebut")
    public String dateDebut;

    @Column(name = "tempActuelle")
    public Long tempActuelle;

    @Column(name = "tMinMatin")
    public Long tMinMatin;

    @Column(name = "tMaxMatin")
    public Long tMaxMatin;

    @Column(name = "nebulositeMatin")
    public Integer nebulositeMatin;

    @Column(name = "pluieMatin")
    public Double pluieMatin;

    @Column(name = "tMinApresMidi")
    public Long tMinApresMidi;

    @Column(name = "tMaxApresMidi")
    public Long tMaxApresMidi;

    @Column(name = "nebulositeApresMidi")
    public Integer nebulositeApresMidi;

    @Column(name = "pluieApresMidi")
    public Double pluieApresMidi;

    public DayPrevision() {
        super();
    }

    public String getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(String idLocation) {
        this.idLocation = idLocation;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Long getTempActuelle() {
        return tempActuelle;
    }

    public String getStringTempActuelle() {
        if(tempActuelle != null){
            return tempActuelle.toString() + "°";
        } else {
            return "";
        }
    }

    public void setTempActuelle(Long tempActuelle) {
        this.tempActuelle = tempActuelle;
    }

    public Long gettMinMatin() {
        return tMinMatin;
    }

    public String getStringTMinMatin(){
        if(tMinMatin != null){
            return tMinMatin.toString();
        } else {
            return "";
        }
    }

    public void settMinMatin(Long tMinMatin) {
        this.tMinMatin = tMinMatin;
    }

    public Long gettMaxMatin() {
        return tMaxMatin;
    }

    public String getStringTMaxMatin() {
        if(tMaxMatin != null){
            return tMaxMatin.toString();
        } else {
            return "";
        }
    }

    public void settMaxMatin(Long tMaxMatin) {
        this.tMaxMatin = tMaxMatin;
    }

    public Integer getNebulositeMatin() {
        return nebulositeMatin;
    }

    public void setNebulositeMatin(Integer nebulositeMatin) {
        this.nebulositeMatin = nebulositeMatin;
    }

    public Double getPluieMatin() {
        return pluieMatin;
    }

    public String getStringPluieMatin() {
        if(pluieMatin != null){
            return pluieMatin.toString();
        } else {
            return "";
        }
    }

    public void setPluieMatin(Double pluieMatin) {
        // Gestion du nombre de décimal pour les chiffres à virgule
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        twoDForm.setDecimalFormatSymbols(dfs);
        this.pluieMatin = Double.valueOf(twoDForm.format(pluieMatin));
    }

    public Long gettMinApresMidi() {
        return tMinApresMidi;
    }

    public String getStringTMinApresMidi() {
        if(tMinApresMidi != null){
            return tMinApresMidi.toString();
        } else {
            return "";
        }
    }

    public void settMinApresMidi(Long tMinApresMidi) {
        this.tMinApresMidi = tMinApresMidi;
    }

    public Long gettMaxApresMidi() {
        return tMaxApresMidi;
    }

    public String getStringTMaxApresMidi() {
        if(tMaxApresMidi != null){
            return tMaxApresMidi.toString();
        } else {
            return "";
        }
    }

    public void settMaxApresMidi(Long tMaxApresMidi) {
        this.tMaxApresMidi = tMaxApresMidi;
    }

    public Integer getNebulositeApresMidi() {
        return nebulositeApresMidi;
    }

    public void setNebulositeApresMidi(Integer nebulositeApresMidi) {
        this.nebulositeApresMidi = nebulositeApresMidi;
    }

    public Double getPluieApresMidi() {
        return pluieApresMidi;
    }

    public String getStringPluieApresMidi() {
        if(pluieApresMidi != null) {
            return pluieApresMidi.toString();
        } else {
            return "";
        }
    }

    public void setPluieApresMidi(Double pluieApresMidi) {
        // Gestion du nombre de décimal pour les chiffres à virgule
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        twoDForm.setDecimalFormatSymbols(dfs);
        this.pluieApresMidi = Double.valueOf(twoDForm.format(pluieApresMidi));
    }

    // Méthode qui permet de connaître les conditions météos du matin en fonction des différentes données de l'objet
    // La recherche de la condition est à titre indicatif, elle n'est pas forcément conforme à la réalité car il nous manque des infos
    public String getConditionMatin(){
        String condition = null;
        if(nebulositeMatin >= 0 && nebulositeMatin < 20 && pluieMatin == 0) {
            condition = "Ensoleillé";
        } else if(nebulositeMatin > 0 && nebulositeMatin < 70 && pluieMatin < 0.5){
            condition = "Peu nuageux";
        } else if(nebulositeMatin >= 70 && nebulositeMatin < 100 && pluieMatin < 0.5) {
            condition = "Nuageux";
        } else if(nebulositeMatin >= 100 && pluieMatin < 0.5){
            condition = "Très nuageux";
        } else if(nebulositeMatin >= 0 && pluieMatin < 1){
            condition = "Faible risque de pluie";
        } else if(nebulositeMatin >= 0 && pluieMatin >= 1){
            condition = "Pluie";
        }

        return condition;
    }

    // Méthode qui permet de connaître les conditions météos de l'après-midi en fonction des différentes données de l'objet
    // La recherche de la condition est à titre indicatif, elle n'est pas forcément conforme à la réalité car il nous manque des infos
    public String getConditionApresMidi(){
        String condition = null;
        if(nebulositeApresMidi >= 0 && nebulositeApresMidi < 20 && pluieApresMidi == 0) {
            condition = "Ensoleillé";
        } else if(nebulositeApresMidi > 0 && nebulositeApresMidi < 70 && pluieApresMidi < 0.5){
            condition = "Peu nuageux";
        } else if(nebulositeApresMidi >= 70 && nebulositeApresMidi < 100 && pluieApresMidi < 0.5) {
            condition = "Nuageux";
        } else if(nebulositeApresMidi >= 100 && pluieApresMidi < 0.5){
            condition = "Très nuageux";
        } else if(nebulositeApresMidi >= 0 && pluieApresMidi < 1){
            condition = "Faible risque de pluie";
        } else if(nebulositeApresMidi >= 0 && pluieApresMidi >= 1){
            condition = "Pluie";
        }

        return condition;
    }
}
