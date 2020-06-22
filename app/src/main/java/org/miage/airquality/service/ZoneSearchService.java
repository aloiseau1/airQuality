package org.miage.airquality.service;

import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.miage.airquality.event.EventBusManager;
import org.miage.airquality.event.zoneevent.SearchZonesEvent;
import org.miage.airquality.event.zoneevent.SearchZonesForMapEvent;
import org.miage.airquality.event.locationevent.SearchLocationsEvent;
import org.miage.airquality.event.locationevent.SearchLocationsForMapEvent;
import org.miage.airquality.model.location.Coordinates;
import org.miage.airquality.model.responsezone.Zone;
import org.miage.airquality.model.location.Location;
import org.miage.airquality.model.location.LocationMeasurement;
import org.miage.airquality.model.responsemeasurements.Measurements;
import org.miage.airquality.model.responsemeasurements.ResponseMeasurements;
import org.miage.airquality.model.location.Parameter;
import org.miage.airquality.model.responsezone.ResponseZone;
import org.miage.airquality.model.responsemeasurements.ResultsMeasurement;
import org.miage.airquality.model.responselocation.ResultsLocation;
import org.miage.airquality.model.responselocation.ResponseLocation;
import org.miage.airquality.model.responselocation.ResponseOneLocation;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Service qui gère les appels à l'API Zone, Location, et Measurement
public class ZoneSearchService {

    public static ZoneSearchService INSTANCE = new ZoneSearchService();
    private final ZoneSearchRESTService mZoneSearchRESTService;

    // Variable permettant de contoler les appels à la BDD et à ne pas recharger les éléments de la vue après chaque réponse d'une API
    private Integer totalMeasurement;
    private Integer totalDayPrevision;
    private Integer totalLocation;
    private Integer totalZone;
    private Integer totalElementLocation;
    private Integer totalElementZone;

    // Variable permettant de sauvegarder les paramètres de recherche rentré par l'utilisateur
    private String searchZone;
    private String searchLocation;
    private Double searchBc;
    private Double searchO3;
    private Double searchCo;
    private Double searchNo2;
    private Double searchSo2;
    private Double searchPm10;
    private Double searchPm25;

    private ZoneSearchService() {
        // Create GSON Converter that will be used to convert from JSON to Java
        Gson gsonConverter = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation().create();

        // Create Retrofit client
        Retrofit retrofit = new Retrofit.Builder()
                // Using OkHttp as HTTP Client
                .client(new OkHttpClient())
                // Having the following as server URL
                .baseUrl("https://api.openaq.org/v1/")
                // Using GSON to convert from Json to Java
                .addConverterFactory(GsonConverterFactory.create(gsonConverter))
                .build();

        // Use retrofit to generate our REST service code
        mZoneSearchRESTService = retrofit.create(ZoneSearchRESTService.class);
    }

    // Méthode qui gère l'appel à l'API Zone
    public void searchZoneFromCountry(final Context context, final String country, final Number limit, final String searchZone, final Boolean map) {
        // On va directement récupérer les données en BDD si le téléphone n'est pas connecté à internet
        if(UtilsService.getInstance(context).isOnline()) {
            // Appel à l'API Zone
            mZoneSearchRESTService.searchZoneByCountry(country, limit).enqueue(new Callback<ResponseZone>() {
                @Override
                public void onResponse(Call<ResponseZone> call, retrofit2.Response<ResponseZone> response) {

                    if (response.body() != null && response.body().results != null) {
                        // Après la récupération des données de l'API
                        // Sauvegarde de chaque zone en BDD, sans les coordonnées
                        ActiveAndroid.beginTransaction();
                        for (Zone zone : response.body().results) {
                            zone.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();

                        // Instanciation des variables permettant de gérer l'affichage des données dans la vue
                        totalZone = response.body().results.size();
                        totalElementZone = 0;

                        // Appel à l'API Location pour récupérer les coordonnées des zones
                        searchLocationFromCityLimit1ForZone(country, response.body().results, searchZone, map);
                    }

                    // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Zone
                    searchInBDDZone(true, searchZone, map);
                }

                @Override
                public void onFailure(Call<ResponseZone> call, Throwable t) {
                    // En cas d'echec, appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Zone
                    searchInBDDZone(true, searchZone, map);
                }
            });
        } else {
            searchInBDDZone(true, searchZone, map);
        }
    }

    // Méthode qui gère l'appel à l'API Location pour récupérer les coordonnées des zones
    public void searchLocationFromCityLimit1ForZone(final String country, final List<Zone> zones, final String searchZone, final Boolean map) {
        // Appel à l'API pour chacune des zones et récupération d'une seule location de cette zone
        for(Zone zone : zones) {
            // Appel à l'API location
            mZoneSearchRESTService.searchLocationByCity(country, zone.getName(), 1, "location").enqueue(new Callback<ResponseLocation>() {
                @Override
                public void onResponse(Call<ResponseLocation> call, retrofit2.Response<ResponseLocation> response) {
                    totalElementZone++;

                    //A chaque réponse de l'API, on insère les coordonnées de la location récupéré dans la zone concernée
                    if (response.body() != null && response.body().results != null) {

                        ResultsLocation resultsLocation = response.body().results.get(0);
                        zone.setLatitude(resultsLocation.getCoordinates().getLatitude());
                        zone.setLongitude(resultsLocation.getCoordinates().getLongitude());
                        zone.save();
                    }

                    // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Zone
                    searchInBDDZone(false, searchZone, map);
                }

                @Override
                public void onFailure(Call<ResponseLocation> call, Throwable t) {
                    totalElementZone++;
                    // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Zone
                    searchInBDDZone(false, searchZone, map);
                }
            });
        }
    }

    // Méthode qui gère l'appel à l'API Location, récupère toute les locations en fonction du nom d'une zone (paramètre city)
    // et également en fonction de la recherche efféctuée sur les locations (paramètre search)
    public void searchLocationFromCity(final Context context, final String country, final String city, final String search, final Boolean map) {
        // On va directement récupérer les données en BDD si le téléphone n'est pas connecté à internet
        if(UtilsService.getInstance(context).isOnline()) {
            // Appel à l'API Location
            mZoneSearchRESTService.searchLocationByCity(country, city, 10000, "location").enqueue(new Callback<ResponseLocation>() {
                @Override
                public void onResponse(Call<ResponseLocation> call, retrofit2.Response<ResponseLocation> response) {
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        List<Location> locationList = new ArrayList<>();

                        ActiveAndroid.beginTransaction();
                        // Pour chaque location trouvée, on les inserent en BDD et ont les ajoutent à une liste qui sera par la suite
                        // envoyée aux autres méthodes pour récupérer les autres données d'une location
                        for (ResultsLocation resultsLocation : response.body().results) {
                            if (UtilsService.INSTANCE.searchValueInArray(resultsLocation.getCities(), city)) {
                                Location oldLocation = searchInBDDLocationByIdLocation(resultsLocation.getId());

                                Location location = saveLocation(resultsLocation, oldLocation, city);
                                locationList.add(location);
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();

                        // Instanciation des variables permettant de gérer l'affichage des données dans la vue
                        totalDayPrevision = 0;
                        totalMeasurement = 0;
                        totalLocation = locationList.size();

                        // Appel aux autres méthodes des API pour récupérer les données supplémentaires des locations
                        MeteoSearchService.INSTANCE.searchMeteoPrevisionFromLatitudeLongitude(locationList, city, search, false, false, map);
                        searchMeasurementFromLocation(locationList, city, search, false, false, map);
                    }

                    // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Location
                    searchInBDDLocation(city, true, search, map);
                }

                @Override
                public void onFailure(Call<ResponseLocation> call, Throwable t) {
                    // En cas d'echec, appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Location
                    searchInBDDLocation(city, true, search, map);
                }
            });
        } else {
            searchInBDDLocation(city, true, search, map);
        }
    }

    // Méthode qui gère l'appel à l'API Location, récupère toute les locations en fonction du nom d'une zone (paramètre city)
    // et également en fonction de la recherche efféctuée sur les locations (paramètre search)
    public void searchLocationFromFavoris(final Context context, String search) {
        // On va directement récupérer les données en BDD si le téléphone n'est pas connecté à internet
        if(UtilsService.getInstance(context).isOnline()) {
            // Recupération des locations en favoris en BDD
            List<Location> locations = searchInBDDLocationByFavoris(true, search);

            for (Location oldLocation : locations) {
                String idLocation = oldLocation.getIdLocation();
                // Appel à l'API Location mais avec recherche par idLocation, donc une seule location retournée par l'API
                mZoneSearchRESTService.searchLocationById(idLocation).enqueue(new Callback<ResponseOneLocation>() {
                    @Override
                    public void onResponse(Call<ResponseOneLocation> call, retrofit2.Response<ResponseOneLocation> response) {
                        totalElementLocation++;
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            ResultsLocation resultsLocation = response.body().results;
                            ActiveAndroid.beginTransaction();

                            // Sauvegarde des données de la location
                            saveLocation(resultsLocation, oldLocation, null);
                        }

                        // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Location
                        searchInBDDLocationByFavoris(false, search);
                    }

                    @Override
                    public void onFailure(Call<ResponseOneLocation> call, Throwable t) {
                        // Request has failed or is not at expected format
                        // We may want to display a warning to user (e.g. Toast)
                        totalElementLocation++;
                    }
                });
            }

            // Instanciation des variables permettant de gérer l'affichage des données dans la vue
            totalLocation = locations.size();
            totalDayPrevision = 0;
            totalMeasurement = 0;
            totalElementLocation = 0;

            // Appel aux autres méthodes des API pour récupérer les données supplémentaires des locations favorites
            MeteoSearchService.INSTANCE.searchMeteoPrevisionFromLatitudeLongitude(locations, null, search, true, false, false);
            searchMeasurementFromLocation(locations, null, search, true, false, false);
        } else {
            // Si pas internet alors appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat
            // de l'API Location
            searchInBDDLocationByFavoris(true, search);
        }
    }

    // Méthode qui gère l'appel à l'API Location, récupère toute les locations en fonction des paramètres saisies dans la fonctionnalité
    // Recherche
    public void searchLocationFromParameter(Context context, String searchZone, String searchLocation, Double searchBc, Double searchNo2,
                                            Double searchSo2, Double searchO3, Double searchCo, Double searchPm25, Double searchPm10) {
        //Sauvegarde de tous les paramètres
        saveParameter(searchZone, searchLocation, searchBc, searchNo2, searchSo2, searchO3, searchCo, searchPm25, searchPm10);

        // On va directement récupérer les données en BDD si le téléphone n'est pas connecté à internet
        if(UtilsService.getInstance(context).isOnline()) {

            // Recupération des locations en BDD en fonction des paramètres renseignés par l'utilisateur dans le formulaire de la
            // fonctionnalité Recherche
            List<Location> locations = searchInBDDLocationByParameter(true);


            for (Location oldLocation : locations) {
                String idLocation = oldLocation.getIdLocation();
                // Appel à l'API Location mais avec recherche par idLocation, donc une seule location retournée par l'API
                mZoneSearchRESTService.searchLocationById(idLocation).enqueue(new Callback<ResponseOneLocation>() {
                    @Override
                    public void onResponse(Call<ResponseOneLocation> call, retrofit2.Response<ResponseOneLocation> response) {
                        totalElementLocation++;
                        // Post an event so that listening activities can update their UI
                        if (response.body() != null && response.body().results != null) {
                            ResultsLocation resultsLocation = response.body().results;

                            // Sauvegarde des données de la location
                            saveLocation(resultsLocation, oldLocation, null);
                        }

                        // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Location
                        searchInBDDLocationByParameter(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseOneLocation> call, Throwable t) {
                        totalElementLocation++;
                        // Appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat de l'API Location
                        searchInBDDLocationByParameter(false);
                    }
                });
            }

            // Instanciation des variables permettant de gérer l'affichage des données dans la vue
            totalLocation = locations.size();
            totalDayPrevision = 0;
            totalMeasurement = 0;
            totalElementLocation = 0;

            // Appel aux autres méthodes des API pour récupérer les données supplémentaires des locations recherchées
            MeteoSearchService.INSTANCE.searchMeteoPrevisionFromLatitudeLongitude(locations, null, null, false, true, false);
            searchMeasurementFromLocation(locations, null, null, false, true, false);
        } else {
            // Si pas internet alors appel à la méthode de recherche des données en BDD pour envoyer une partie du résultat
            // de l'API Location
            searchInBDDLocationByParameter(true);
        }
    }

    // Méthode qui gère l'appel à l'API Measurement, récupère les dernières mesures pour chacune des locations reçues en paramètres
    // locationList : Liste des locations pour lesquelles un appel à l'API Measurement va être réalisé
    // city : nom de la zone lié à la demande de résultat en cours
    // search : chaîne de caractère correspondant à la recherche d'une location précise
    // favoris : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Favoris
    // recherche : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Recherche
    // map : booléen qui permet de savoir si la méthode est appelée pour la fonctionnalité Map
    public void searchMeasurementFromLocation(List<Location> locationList, String city, String search, Boolean favoris, Boolean recherche, Boolean map) {
        for (Location location: locationList) {
            // Appel à l'API Measurement pour une location
            mZoneSearchRESTService.searchLatestMeasurementByLocation(location.getLocation()).enqueue(new Callback<ResponseMeasurements>() {
                @Override
                public void onResponse(Call<ResponseMeasurements> call, retrofit2.Response<ResponseMeasurements> response) {
                    totalMeasurement++;
                    // Post an event so that listening activities can update their UI
                    if (response.body() != null && response.body().results != null) {
                        Measurements measurements = response.body().results.get(0);

                        ActiveAndroid.beginTransaction();
                        // Insertion des données de chaque mesure en BDD
                        for (ResultsMeasurement resultsMeasurement : measurements.getResultMeasurements()) {
                            LocationMeasurement measurement = new LocationMeasurement();
                            measurement.setIdLocation(location.getIdLocation());
                            measurement.setParameter(resultsMeasurement.getParameter());
                            measurement.setValue(resultsMeasurement.getValue());
                            measurement.setUnit(resultsMeasurement.getUnit());
                            measurement.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                    }

                    // Appel au méthode de recherche du nouveau contenu en BDD en fonction de la fonctionnalité demandée (cf. donnée en paramètre)
                    if(favoris){
                        searchInBDDLocationByFavoris(false, search);
                    } else if(recherche) {
                        ZoneSearchService.INSTANCE.searchInBDDLocationByParameter(false);
                    } else {
                        searchInBDDLocation(city, false, search, map);
                    }
                }

                @Override
                public void onFailure(Call<ResponseMeasurements> call, Throwable t) {
                    totalMeasurement++;

                    // En cas d'erreur de l'appel à l'API, appel à la méthode de recherche du nouveau contenu en BDD
                    // en fonction de la fonctionnalité demandée (cf. donnée en paramètre)
                    if(favoris){
                        searchInBDDLocationByFavoris(false, search);
                    } else if(recherche) {
                        ZoneSearchService.INSTANCE.searchInBDDLocationByParameter(false);
                    } else {
                        searchInBDDLocation(city, false, search, map);
                    }
                }
            });
        }
    }

    // Sauvegarde de toutes les données d'une location qui sont récupérées par l'API Location
    private Location saveLocation(ResultsLocation resultsLocation, Location oldLocation, String city){
        ActiveAndroid.beginTransaction();

        // Création d'une location
        Location location = new Location();
        location.setIdLocation(resultsLocation.getId());
        location.setCity(resultsLocation.getCity());
        location.setLocation(resultsLocation.getLocation());
        if(city != null) {
            location.setNameZone(city);
        } else {
            location.setNameZone(oldLocation.getNameZone());
        }
        if(oldLocation != null) {
            location.setFavoris(oldLocation.getFavoris());
        } else {
            location.setFavoris(false);
        }
        location.save();

        List<Parameter> parameterList = new ArrayList<>();
        //Création des paramètres pris en compte par la location
        for(String stringParameter: resultsLocation.getParameters()){
            Parameter parameter = new Parameter();
            parameter.setIdLocation(resultsLocation.getId());
            parameter.setParameter(stringParameter);
            parameter.save();
            parameterList.add(parameter);
        }
        location.setParameters(parameterList);

        // Création des coordonnées pour la location
        Coordinates coordinates = new Coordinates();
        coordinates.setIdLocation(resultsLocation.getId());
        coordinates.setLatitude(resultsLocation.getCoordinates().getLatitude());
        coordinates.setLongitude(resultsLocation.getCoordinates().getLongitude());
        coordinates.save();
        location.setCoordinates(coordinates);

        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
        return location;
    }

    // Sauvegarde des paremètres de recherche pour la fonctionnalité Recherche
    private void saveParameter(String searchZone, String searchLocation, Double searchBc, Double searchNo2,
                               Double searchSo2, Double searchO3, Double searchCo, Double searchPm25, Double searchPm10){
        this.searchZone = searchZone;
        this.searchLocation = searchLocation;
        this.searchBc = searchBc;
        this.searchNo2 = searchNo2;
        this.searchSo2 = searchSo2;
        this.searchO3 = searchO3;
        this.searchCo = searchCo;
        this.searchPm25 = searchPm25;
        this.searchPm10 = searchPm10;
    }

    // Méthode permettant de savoir si les mesures d'une location respectent les critères de recherche
    private Boolean checkMeasurement(List<LocationMeasurement> locationMeasurements){
        Boolean checkSearchSo2, checkSearchNo2, checkSearchCo, checkSearchBc, checkSearchO3, checkSearchPm10, checkSearchPm25;
        checkSearchBc = searchBc == null;
        checkSearchCo = searchCo == null;
        checkSearchNo2 = searchNo2 == null;
        checkSearchO3 = searchO3 == null;
        checkSearchPm10 = searchPm10 == null;
        checkSearchPm25 = searchPm25 == null;
        checkSearchSo2 = searchSo2 == null;
        Boolean valide = true;
        Integer comptLocationMeasurement = 0;
        if(locationMeasurements.size() > 0 && (searchSo2 != null || searchNo2 != null || searchCo != null || searchBc != null ||
                searchO3 != null || searchPm10 != null || searchPm25 != null)) {

            while (valide && comptLocationMeasurement < locationMeasurements.size()) {
                LocationMeasurement locationMeasurement = locationMeasurements.get(comptLocationMeasurement);
                switch (locationMeasurement.getParameter()) {
                    case "so2":
                        if (searchSo2 != null && searchSo2 > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchSo2 = true;
                        }
                        break;
                    case "no2":
                        if (searchNo2 != null && searchNo2 > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchNo2 = true;
                        }
                        break;
                    case "co":
                        if (searchCo != null && searchCo > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchCo = true;
                        }
                        break;
                    case "bc":
                        if (searchBc != null && searchBc > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchBc = true;
                        }
                        break;
                    case "o3":
                        if (searchO3 != null && searchO3 > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchO3 = true;
                        }
                        break;
                    case "pm10":
                        if (searchPm10 != null && searchPm10 > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchPm10 = true;
                        }
                        break;
                    case "pm25":
                        if (searchPm25 != null && searchPm25 > locationMeasurement.getValue()) {
                            valide = false;
                        } else {
                            checkSearchPm25 = true;
                        }
                        break;
                    default:
                        break;
                }
                comptLocationMeasurement++;
            }

            if(!(checkSearchBc && checkSearchCo && checkSearchNo2 && checkSearchO3 && checkSearchPm10 && checkSearchPm25 && checkSearchSo2)){
                valide = false;
            }
        } else if(searchSo2 != null || searchNo2 != null || searchCo != null || searchBc != null ||
                searchO3 != null || searchPm10 != null || searchPm25 != null){
            valide = false;
        }
        return valide;
    }

    // Méthode permettant d'incrémenter le compteur d'appel à l'API Météo
    // Elle est appelée dans le service Météo
    public void addTotalDayPrevision() {
        totalDayPrevision++;
    }

    // Méthode qui récupère les données des zones en BDD
    public void searchInBDDZone(Boolean first, String search, Boolean map){
        // Envoi les données vers la vue seulement si c'est un premier appel (lors d'un mode hors ligne ou d'une recherche rapide cf. EditText des listes)
        // Ou lorsque tous les appels au API fait pour la fonctionnalité ont été réalisés
        if(first || totalZone.equals(totalElementZone)) {
            List<Zone> zones;
            if (search.equals("")) {
                zones = new Select().
                        from(Zone.class)
                        .orderBy("name")
                        .execute();
            } else {
                zones = new Select().
                        from(Zone.class)
                        .where("name LIKE \"%" + search + "%\"")
                        .orderBy("name")
                        .execute();
            }
            UtilsService.INSTANCE.visibilityGone = (first && !UtilsService.INSTANCE.isOnline()) || (!first && UtilsService.INSTANCE.isOnline());
            if(map){
                EventBusManager.BUS.post(new SearchZonesForMapEvent(zones));
            } else {
                EventBusManager.BUS.post(new SearchZonesEvent(zones));
            }
        }
    }

    // Méthode qui récupère les données des Locations en BDD
    public void searchInBDDLocation(String city, Boolean first, String search, Boolean map){
        // Envoi les données vers la vue seulement si c'est un premier appel (lors d'un mode hors ligne ou d'une recherche rapide cf. EditText des listes)
        // Ou lorsque tous les appels au API fait pour la fonctionnalité ont été réalisés
        if(first || (totalDayPrevision.equals(totalLocation) && totalMeasurement.equals(totalLocation))) {
            List<Location> locations;
            if(search == null){
                locations = new Select().
                        from(Location.class)
                        .where("nameZone LIKE \"" + city + "\"")
                        .orderBy("city ASC")
                        .execute();
            } else {
                locations = new Select().
                        from(Location.class)
                        .where("nameZone LIKE \"" + city + "\"")
                        .and("city LIKE \"%" + search + "%\"")
                        .orderBy("city ASC")
                        .execute();
            }
            UtilsService.INSTANCE.visibilityGone = (first && !UtilsService.INSTANCE.isOnline()) || (!first && UtilsService.INSTANCE.isOnline());
            if(map){
                EventBusManager.BUS.post(new SearchLocationsForMapEvent(locations));
            } else {
                EventBusManager.BUS.post(new SearchLocationsEvent(locations));
            }
        }
    }

    // Méthode qui récupère les données des Locations favorites en BDD
    public List<Location> searchInBDDLocationByFavoris(Boolean first, String search){
        // Envoi les données vers la vue seulement si c'est un premier appel (lors d'un mode hors ligne ou d'une recherche rapide cf. EditText des listes)
        // Ou lorsque tous les appels au API fait pour la fonctionnalité ont été réalisés
        if(first || (totalElementLocation.equals(totalLocation) && totalMeasurement.equals(totalLocation) && totalDayPrevision.equals(totalLocation))) {
            List<Location> locations;
            if(search == null){
                locations = new Select().
                        from(Location.class)
                        .where("favoris")
                        .orderBy("city ASC")
                        .execute();
            } else {
                locations = new Select().
                        from(Location.class)
                        .where("favoris")
                        .and("city LIKE \"%" + search + "%\"")
                        .orderBy("city ASC")
                        .execute();
            }

            UtilsService.INSTANCE.visibilityGone = (first && !UtilsService.INSTANCE.isOnline()) || (!first && UtilsService.INSTANCE.isOnline());
            EventBusManager.BUS.post(new SearchLocationsEvent(locations));
            return locations;
        } else {
            return new ArrayList<>();
        }
    }

    // Méthode qui récupère les données des Locations en BDD en fonction des paramètres de recherche de la fonctionnalité Recherche
    public List<Location> searchInBDDLocationByParameter(Boolean first){
        // Envoi les données vers la vue seulement si c'est un premier appel (lors d'un mode hors ligne ou d'une recherche rapide cf. EditText des listes)
        // Ou lorsque tous les appels au API fait pour la fonctionnalité ont été réalisés
        if(first || (totalElementLocation.equals(totalLocation) && totalMeasurement.equals(totalLocation) && totalDayPrevision.equals(totalLocation))) {
            List<Location> locations = new ArrayList<>();
            From query = new Select().
                    from(Location.class);
            if(searchZone != null){
                query.where("nameZone LIKE '%" + searchZone + "%'");
            }
            if(searchLocation != null){
                query.where("city LIKE '%" + searchLocation + "%'");
            }
            List<Location> tempLocations = query.orderBy("city ASC").limit(50).execute();

            for (int i = 0; i < tempLocations.size(); i++) {
                Location location = tempLocations.get(i);
                Boolean valide = checkMeasurement(location.getMeasurements());
                if(valide) {
                    locations.add(location);
                }
            }
            UtilsService.INSTANCE.visibilityGone = (first && !UtilsService.INSTANCE.isOnline()) || (!first && UtilsService.INSTANCE.isOnline());
            EventBusManager.BUS.post(new SearchLocationsEvent(locations));
            return locations;
        } else {
            return new ArrayList<>();
        }
    }

    // Méthode qui récupère les données d'une location en fonction de son idLocation
    public Location searchInBDDLocationByIdLocation(String idLocation){
        return new Select().
                from(Location.class)
                .where("idLocation LIKE \"" + idLocation + "\"")
                .executeSingle();
    }

    // Service describing the REST APIs
    public interface ZoneSearchRESTService {

        @GET("cities?")
        Call<ResponseZone> searchZoneByCountry(@Query("country") String search, @Query("limit") Number limit);

        @GET("locations?")
        Call<ResponseLocation> searchLocationByCity(@Query("country") String country, @Query("city") String city, @Query("limit") Number limit, @Query("order_by") String orderBy);

        @GET("locations/{id}")
        Call<ResponseOneLocation> searchLocationById(@Path(value = "id", encoded = true) String id);

        @GET("latest?")
        Call<ResponseMeasurements> searchLatestMeasurementByLocation(@Query("location") String location);
    }
}
