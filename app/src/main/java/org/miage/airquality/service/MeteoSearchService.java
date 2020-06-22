package org.miage.airquality.service;

import com.activeandroid.ActiveAndroid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.miage.airquality.model.location.DayPrevision;
import org.miage.airquality.model.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

// Service lié à la gestion des appels vers l'API météo
public class MeteoSearchService {

    public static MeteoSearchService INSTANCE = new MeteoSearchService();
    private final MeteoSearchRESTService mMeteoSearchRESTService;

    // Variable utilisée pour le traitement de gestion des données retournées par l'API météo
    private String dateActuelle;
    private String heureActuelle;

    // Variable d'authentification pour l'API météo (à changer si l'authentification a changée)
    private String token = "VE4DFAZ4VHYCL1BnUiQFLARsU2YLfQkuVytRMg9qA34GbVAxVTUGYFI8AXwHKFFnWHVQMwE6V2cGbQJ6WykHZlQ%2BA28GbVQzAm1QNVJ9BS4EKlMyCysJLlc9UTQPfANhBmZQPVUoBmVSOAFnBylRZFhpUDIBIVdwBmQCYFs%2BB2JUNwNuBmNUMAJqUDFSfQUuBDJTZgs8CThXYVFnD2ADYQZhUD1VNAZkUj4BYwcpUWRYbFA1AThXbgZiAmBbMQd7VCgDHgYWVCsCLVBwUjcFdwQqU2YLagll";
    private String signature = "a00a1bf8d574275f3b3e19f3db1e6895";

    private MeteoSearchService() {
        // Create GSON Converter that will be used to convert from JSON to Java
        Gson gsonConverter = new GsonBuilder()
                .create();

        // Create Retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                // Using OkHttp as HTTP Client
                .client(new OkHttpClient())
                // Having the following as server URL
                .baseUrl("https://www.infoclimat.fr/public-api/gfs/")
                // Using GSON to convert from Json to Java
                .addConverterFactory(GsonConverterFactory.create(gsonConverter))
                .build();

        // Use retrofit to generate our REST service code
        mMeteoSearchRESTService = retrofit.create(MeteoSearchRESTService.class);

        // Récupération de la date et l'heure du jour
        Date actuelle = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormatHour = new SimpleDateFormat("HH");
        dateActuelle = dateFormat.format(actuelle);
        heureActuelle = dateFormatHour.format(actuelle);
    }

    // Méthode qui gère l'appel à l'API météo, elle prend en compte plusieurs paramètres :
    // locationList : Liste des locations pour lesquelles un appel à l'API météo va être réalisé
    // city : nom de la zone lié à la demande de résultat en cours
    // search : chaîne de caractère correspondant à la recherche d'une location précise
    // favoris : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Favoris
    // recherche : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Recherche
    // map : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Map
    public void searchMeteoPrevisionFromLatitudeLongitude(List<Location> locationList, String city, String search, Boolean favoris, Boolean recherche, Boolean map) {
        for (Location location: locationList) {
            String coordinate = location.getCoordinates().getLatitude() + "," + location.getCoordinates().getLongitude();
            // Appel à l'API météo en fonction des coordonnées d'une location
            mMeteoSearchRESTService.searchMeteoPrevisionFromLatitudeLongitude(coordinate, this.token,this.signature).enqueue(new Callback<JsonObject>() {
                // Récupération d'un JsonObject (pas de modèle en particulier cf. rapport)
                @Override
                public void onResponse(Call<JsonObject> call, retrofit2.Response<JsonObject> response) {
                    ZoneSearchService.INSTANCE.addTotalDayPrevision();

                    if (response.isSuccessful()) {
                        try {
                            // Création de chaque jour de prévision météo à partir de la réponse de l'API
                            // Utilisation du pattern Iterator pour faciliter le parcours de l'objet JSON
                            ActiveAndroid.beginTransaction();
                            JSONObject jsonResult = new JSONObject(new Gson().toJson(response.body()));
                            Iterator<String> keys = jsonResult.keys();
                            String dateTemp = "";

                            // Création des jours de prévision pour la location
                            DayPrevision dayPrevision = new DayPrevision();
                            while(keys.hasNext()){
                                String key = keys.next();
                                if(jsonResult.get(key) instanceof JSONObject){
                                    // Récupération de la date et de l'heure du jour de prévision météo à partir du noeud de l'objet du JSON
                                    String date = key.substring(0,10);
                                    String hour = key.substring(11,13);

                                    // Récupération des données importantes d'un des noeuds du JSON
                                    JSONObject jsonObjectDate = (JSONObject)jsonResult.get(key);
                                    Long temperature = Math.round(jsonObjectDate.getJSONObject("temperature").getDouble("sol") - 273.15);
                                    Integer nebulosite = jsonObjectDate.getJSONObject("nebulosite").getInt("totale");
                                    Double pluie = jsonObjectDate.getDouble("pluie");

                                    // Modification de l'objet DayPrevision à partir du noeud JSON si la date du noeud correspond au jour de prévision météo en cours
                                    if(dateTemp.equals(date)){
                                        // Gestion de la saisie de la température actuelle
                                        if(date.equals(dateActuelle) && Integer.parseInt(hour) >= Integer.parseInt(heureActuelle)){
                                            dayPrevision.setTempActuelle(temperature);
                                        }
                                        if(Integer.parseInt(hour) >= 12){
                                            if(dayPrevision.gettMaxApresMidi() == null && dayPrevision.gettMinApresMidi() == null){
                                                dayPrevision.settMinApresMidi(temperature);
                                                dayPrevision.settMaxApresMidi(temperature);
                                            } else if(dayPrevision.gettMinApresMidi() > temperature){
                                                dayPrevision.settMinApresMidi(temperature);
                                            } else if(dayPrevision.gettMaxApresMidi() < temperature){
                                                dayPrevision.settMaxApresMidi(temperature);
                                            }

                                            if(dayPrevision.getNebulositeApresMidi() < nebulosite){
                                                dayPrevision.setNebulositeApresMidi(nebulosite);
                                            }
                                            dayPrevision.setPluieApresMidi(pluie + dayPrevision.getPluieApresMidi());
                                        } else {
                                            if(dayPrevision.gettMinMatin() > temperature){
                                                dayPrevision.settMinMatin(temperature);
                                            } else if(dayPrevision.gettMaxMatin() < temperature){
                                                dayPrevision.settMaxMatin(temperature);
                                            }

                                            if(dayPrevision.getNebulositeMatin() < nebulosite){
                                                dayPrevision.setNebulositeMatin(nebulosite);
                                            }
                                            dayPrevision.setPluieMatin(pluie + dayPrevision.getPluieMatin());
                                        }
                                    } else {
                                        // Création du premier jour de prévision météo correspondant à la date du jour
                                        if(date.equals(dateActuelle)){
                                            dateTemp = date;
                                            dayPrevision = newDayPrevision(location.getIdLocation(), date, temperature, nebulosite, pluie);
                                        } else if(!dateTemp.equals("")){
                                            // Sauvegarde du jour de prévision météo dans le cas ou la date du nouveau noeud JSON ne correspond pas
                                            // à la date conservé dans l'objet DayPrevision
                                            dayPrevision.save();
                                            dateTemp = date;
                                            // Création d'un nouvel objet DayPrevision à partir du premier noeud de la nouvelle date de prévision météo
                                            dayPrevision = newDayPrevision(location.getIdLocation(), date, temperature, nebulosite, pluie);
                                        }
                                    }
                                }
                            }
                            dayPrevision.save(); // Sauvegarde du dernière élément du JSON

                            ActiveAndroid.setTransactionSuccessful();
                            ActiveAndroid.endTransaction();

                        } catch (JSONException e){
                            e.printStackTrace();
                            ActiveAndroid.endTransaction();
                        }
                    }

                    // Appel au méthode de recherche du nouveau contenu en BDD en fonction de la fonctionnalité demandée (cf. donnée en paramètre)
                    if(favoris){
                        ZoneSearchService.INSTANCE.searchInBDDLocationByFavoris(false, search);
                    } else if(recherche) {
                        ZoneSearchService.INSTANCE.searchInBDDLocationByParameter(false);
                    } else {
                        ZoneSearchService.INSTANCE.searchInBDDLocation(city,false, search, map);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    ZoneSearchService.INSTANCE.addTotalDayPrevision();

                    // En cas d'erreur de l'appel à l'API, appel à la méthode de recherche du nouveau contenu en BDD
                    // en fonction de la fonctionnalité demandée (cf. donnée en paramètre)
                    if(favoris){
                        ZoneSearchService.INSTANCE.searchInBDDLocationByFavoris(false, search);
                    } else if(recherche) {
                        ZoneSearchService.INSTANCE.searchInBDDLocationByParameter(false);
                    } else {
                        ZoneSearchService.INSTANCE.searchInBDDLocation(city,false, search, map);
                    }
                }
            });
        }
    }

    // Méthode qui permet de créer un nouvel Objet DayPrevision à partir du premier noeud JSON d'une date
    private DayPrevision newDayPrevision(String idLocation, String date, Long temperature, Integer nebulosite, Double pluie){
        DayPrevision dayPrevision = new DayPrevision();
        dayPrevision.setIdLocation(idLocation);
        dayPrevision.setDate(date);
        dayPrevision.setDateDebut(dateActuelle);
        dayPrevision.setTempActuelle(null);
        dayPrevision.settMinMatin(temperature);
        dayPrevision.settMaxMatin(temperature);
        dayPrevision.setNebulositeMatin(nebulosite);
        dayPrevision.setPluieMatin(pluie);
        dayPrevision.settMinApresMidi(null);
        dayPrevision.settMaxApresMidi(null);
        dayPrevision.setNebulositeApresMidi(0);
        dayPrevision.setPluieApresMidi(0.0);
        return dayPrevision;
    }

    // Service describing the REST APIs
    public interface MeteoSearchRESTService {

        @GET("json?")
        Call<JsonObject> searchMeteoPrevisionFromLatitudeLongitude(@Query("_ll") String _ll, @Query(value="_auth", encoded = true) String auth, @Query("_c") String signature);
    }
}
