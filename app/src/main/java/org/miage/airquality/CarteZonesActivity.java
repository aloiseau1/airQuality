package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.otto.Subscribe;

import org.miage.airquality.event.EventBusManager;
import org.miage.airquality.event.zoneevent.SearchZonesForMapEvent;
import org.miage.airquality.event.locationevent.SearchLocationsForMapEvent;
import org.miage.airquality.model.responsezone.Zone;
import org.miage.airquality.model.location.Location;
import org.miage.airquality.model.map.MyItem;
import org.miage.airquality.model.location.Parameter;
import org.miage.airquality.service.UtilsService;
import org.miage.airquality.service.ZoneSearchService;

import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Activity qui affiche la carte avec les markers des zones et des locations
public class CarteZonesActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.activity_carte_zone_loader)
    ProgressBar mProgressBar;

    @BindView(R.id.activity_carte_zone_search)
    EditText mCarteSearch;

    private GoogleMap mActiveGoogleMap; // Variable qui contient l'élément GoogleMap

    // Variable contenant les zones et les locations liés à leur item (marker affiché sur la carte)
    private Map<MyItem, Zone> mMarkersToZone = new LinkedHashMap<>();
    private Map<MyItem, Location> mMarkersToLocation = new LinkedHashMap<>();

    private ClusterManager<MyItem> mClusterManager; // Variable qui permet la gestion des clusters sur la carte
    private String zone; // Variable qui contient la zone qui a été sélectionné avant l'affichage des locations concernées
    private Boolean searchProgressBar = false; // Variable qui permet de savoir si on est sur une recherche direct en BDD, elle est utilisé pour afficher ou non la progressBar
    private Boolean flagClearEditText = false; // Variable qui permet de bloquer l'eventListener de l'editText lorsque l'on vide la saisie de de l'EditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carte_zones);

        // Binding ButterKnife annotations now that content view has been set
        ButterKnife.bind(this);

        // Get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_carte_zone_map);
        mapFragment.getMapAsync(this);

        mProgressBar.setVisibility(View.VISIBLE);// Affichage du loader

        mCarteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Nothing to do when texte is about to change
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // While text is changing, hide list and show loader
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Recherche en BDD seulement si la modification de l'editText est faite par l'utilisateur
                if(!flagClearEditText) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    searchProgressBar = true;

                    // Recherche en BDD des zones si on se situe sur l'affichage des zones,
                    // ou recherche en BDD des locations si on se situe sur l'affichage des locations
                    if (mMarkersToZone.size() > 0) {
                        ZoneSearchService.INSTANCE.searchInBDDZone(true, editable.toString(), true);
                    } else if (mMarkersToLocation.size() > 0) {
                        ZoneSearchService.INSTANCE.searchInBDDLocation(zone, true, editable.toString(), true);
                    } else {
                        // Dans le cas où on a aucune donnée, on affiche un message d'erreur
                        mProgressBar.setVisibility(View.GONE);
                        LayoutInflater inflater = getLayoutInflater();
                        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_error);
                        UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "error");
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        // Do NOT forget to call super.onResume()
        super.onResume();

        // Register to Event bus : now each time an event is posted, the activity will receive it if it is @Subscribed to this event
        EventBusManager.BUS.register(this);
    }

    @Override
    protected void onPause() {
        // Unregister from Event bus : if event are posted now, the activity will not receive it
        EventBusManager.BUS.unregister(this);

        // Do NOT forget to call super.onPause()
        super.onPause();
    }

    // Bouton qui permet de revenir sur l'écran initial de la carte avec toutes les zones
    @OnClick(R.id.buttonRefreshCarte)
    public void clickedRefresh() {
        searchProgressBar = true;
        flagClearEditText = true;
        mCarteSearch.getText().clear();
        mCarteSearch.setHint("Recherchez une zone");
        ZoneSearchService.INSTANCE.searchInBDDZone(true, "", true);
        flagClearEditText = false;
    }

    // Méthode qui écoute l'événement SearchZonesForMapEvent
    // Elle gère l'affichage des zones sur la carte
    @Subscribe
    public void searchResultZone(final SearchZonesForMapEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // On gère l'affichage des zones que si la carte est prête
                if (mActiveGoogleMap != null) {
                    if(event.getResultsZones().size() > 0) {
                        mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader

                        // Nettoyage de tous les tableaux des données et de la carte
                        mActiveGoogleMap.clear();
                        mMarkersToZone.clear();
                        mMarkersToLocation.clear();

                        // Création du clusterManager qui va permettre de gérer les clusters
                        mClusterManager = new ClusterManager<MyItem>(getApplicationContext(), mActiveGoogleMap);
                        mActiveGoogleMap.setOnCameraIdleListener(mClusterManager);
                        mActiveGoogleMap.setOnMarkerClickListener(mClusterManager);

                        LatLngBounds.Builder cameraBounds = LatLngBounds.builder(); // Permet de gérer le zoom sur la carte
                        // Insertion de toutes les zones dans le clusterManager pour afficher les zones sur la carte
                        for (Zone zone : event.getResultsZones()) {
                            if (zone.getLatitude() != null && zone.getLongitude() != null) {
                                MyItem myItem = new MyItem(zone.getLatitude(), zone.getLongitude(), zone.getName(), null);

                                cameraBounds.include(myItem.getPosition()); // Ajout des items pour configurer le zoom

                                mClusterManager.addItem(myItem);
                                mMarkersToZone.put(myItem, zone); // Insertion de la liaison item-zone pour gérer le onClusterItemInfoWindowClick
                            }
                        }

                        // Instantiation d'un onClusterClick pour gérer le zoom lors du click sur un cluster
                        mClusterManager
                                .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                                    @Override
                                    public boolean onClusterClick(final Cluster<MyItem> cluster) {
                                        mActiveGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                cluster.getPosition(), (float) Math.floor(mActiveGoogleMap
                                                        .getCameraPosition().zoom + 1)), 300,
                                                null);
                                        return true;
                                    }
                                });

                        // Instantiation d'un onClusterItemInfoWindowClick qui permet de charger les locations de la zone sélectionnée
                        mClusterManager
                                .setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>() {
                                    @Override
                                    public void onClusterItemInfoWindowClick(MyItem item) {
                                        Zone zone = mMarkersToZone.get(item);
                                        CarteZonesActivity.this.zone = zone.getName();

                                        // Configuration et nettoyage de l'EditText pour la recherche de location
                                        flagClearEditText = true;
                                        mCarteSearch.getText().clear();
                                        mCarteSearch.setHint("Recherchez un lieu");
                                        flagClearEditText = false;
                                        mMarkersToZone.clear();

                                        mProgressBar.setVisibility(View.VISIBLE);
                                        ZoneSearchService.INSTANCE.searchLocationFromCity(getBaseContext(), "FR", zone.getName(), "", true);
                                    }
                                });

                        // Dans le cas où l'on a des markers sur la carte, on fait le zoom sur la carte
                        if(mMarkersToZone.size() > 0){
                            mActiveGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraBounds.build().getCenter(),2));
                        }

                        // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
                        // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche le toast success
                        if(UtilsService.INSTANCE.visibilityGone || searchProgressBar) {
                            mProgressBar.setVisibility(View.GONE);
                            if(!searchProgressBar){
                                LayoutInflater inflater = getLayoutInflater();
                                ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success);
                                UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success");
                            }
                            searchProgressBar = false;
                        }
                    } else {
                        // Dans le cas où aucune zone a été reçu par le service, alors on clear la carte et le cluster manager
                        mActiveGoogleMap.clear();
                        mClusterManager.clearItems();

                        // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
                        // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche
                        // le toast warning car on a aucune donnée à afficher
                        if(UtilsService.INSTANCE.visibilityGone || searchProgressBar) {
                            mProgressBar.setVisibility(View.GONE);
                            searchProgressBar = false;

                            LayoutInflater inflater = getLayoutInflater();
                            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_warning);
                            UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "warning");
                        }
                    }
                }
            }
        });
    }

    // Méthode qui écoute l'événement SearchLocationsForMapEvent
    // Elle gère l'affichage des locations sur la carte
    @Subscribe
    public void searchResultLocation(final SearchLocationsForMapEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // On gère l'affichage des locations que si la carte est prête
                if (mActiveGoogleMap != null) {
                    if(event.getLocations().size() > 0) {
                        mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader

                        // Nettoyage de tous les tableaux des données et de la carte
                        mActiveGoogleMap.clear();
                        mMarkersToZone.clear();
                        mMarkersToLocation.clear();

                        // Création du clusterManager qui va permettre de gérer les clusters
                        mClusterManager = new ClusterManager<MyItem>(getApplicationContext(), mActiveGoogleMap);
                        mActiveGoogleMap.setOnCameraIdleListener(mClusterManager);
                        mActiveGoogleMap.setOnMarkerClickListener(mClusterManager);

                        LatLngBounds.Builder cameraBounds = LatLngBounds.builder(); // Permet de gérer le zoom sur la carte
                        // Insertion de toutes les locations dans le clusterManager pour afficher les locations sur la carte
                        for (Location location : event.getLocations()) {
                            if (location.getCoordinates() != null) {
                                // Récupération des données de la location pour les afficher dans l'infoWindow du marker
                                String title = location.getIdLocation() + " - " + location.getCity() + " " + location.getTempActuelle();
                                String measurement = "";
                                for (Parameter parameter : location.getParameters()) {
                                    measurement += location.getOneMeasurement(parameter.getParameter());
                                }
                                MyItem myItem = new MyItem(location.getCoordinates().getLatitude(),
                                        location.getCoordinates().getLongitude(), title, measurement);

                                cameraBounds.include(myItem.getPosition()); // Ajout des items pour configurer le zoom

                                mClusterManager.addItem(myItem);
                                mMarkersToLocation.put(myItem,location);// Insertion de la liaison item-location pour gérer le onClusterItemInfoWindowClick
                            }
                        }

                        // Instantiation d'un onClusterClick pour gérer le zoom lors du click sur un cluster
                        mClusterManager
                                .setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                                    @Override
                                    public boolean onClusterClick(final Cluster<MyItem> cluster) {
                                        mActiveGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                                cluster.getPosition(), (float) Math.floor(mActiveGoogleMap
                                                        .getCameraPosition().zoom + 1)), 300,
                                                null);
                                        return true;
                                    }
                                });

                        // Instantiation d'un onClusterItemInfoWindowClick qui permet d'instancier l'activity Detail Location de la location sélectionnée
                        mClusterManager
                                .setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>() {
                                    @Override
                                    public void onClusterItemInfoWindowClick(MyItem item) {
                                        Location location = mMarkersToLocation.get(item);
                                        Intent seePlaceDetailIntent = new Intent(CarteZonesActivity.this, DetailLocationActivity.class);
                                        seePlaceDetailIntent.putExtra("location", location);
                                        startActivity(seePlaceDetailIntent);
                                    }
                                });

                        // Dans le cas où l'on a des markers sur la carte, on fait le zoom sur la carte
                        if(mMarkersToLocation.size() > 0){
                            mActiveGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraBounds.build().getCenter(),7));
                        }

                        // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
                        // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche le toast success
                        if(UtilsService.INSTANCE.visibilityGone || searchProgressBar) {
                            // Step 2: hide loader
                            mProgressBar.setVisibility(View.GONE);
                            if(!searchProgressBar) {
                                LayoutInflater inflater = getLayoutInflater();
                                ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success);
                                UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success");
                            }
                            searchProgressBar = false;
                        }
                    } else {
                        // Dans le cas où aucune zone a été reçu par le service, alors on clear la carte et le cluster manager
                        mActiveGoogleMap.clear();
                        mClusterManager.clearItems();

                        // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
                        // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche
                        // le toast warning car on a aucune donnée à afficher
                        if(UtilsService.INSTANCE.visibilityGone || searchProgressBar) {
                            // Step 2: hide loader
                            mProgressBar.setVisibility(View.GONE);
                            searchProgressBar = false;

                            LayoutInflater inflater = getLayoutInflater();
                            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_warning);
                            UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "warning");
                        }
                    }
                }
            }
        });
    }

    // Méthode qui initialize la configuration de la MAP une fois qu'elle est prête à être affichée
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mActiveGoogleMap = googleMap;
        mActiveGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mActiveGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // Appel au chargement des données à partir du service ZoneSearch une fois que la map est prête
        ZoneSearchService.INSTANCE.searchZoneFromCountry(this, "FR", 10000, "", true);
    }
}
