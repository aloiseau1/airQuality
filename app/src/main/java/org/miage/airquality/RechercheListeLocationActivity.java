package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.miage.airquality.event.EventBusManager;
import org.miage.airquality.event.locationevent.SearchLocationsEvent;
import org.miage.airquality.service.UtilsService;
import org.miage.airquality.service.ZoneSearchService;
import org.miage.airquality.ui.LocationAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

// Activity qui affiche les locations en fonction des critères de recherche de l'utillisateur
public class RechercheListeLocationActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.activity_recherche_liste_location_loader)
    ProgressBar mProgressBar;

    private LocationAdapter mLocationAdapter;
    private Boolean background = false; // Variable qui permet de savoir si l'activité est en background (en onPause)

    // Variable permettant de sauvegarder les paramètres de recherche rentrés par l'utilisateur
    private String searchZone;
    private String searchLocation;
    private Double searchBc;
    private Double searchO3;
    private Double searchCo;
    private Double searchNo2;
    private Double searchSo2;
    private Double searchPm10;
    private Double searchPm25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche_liste_location);

        ButterKnife.bind(this);

        // Récupération de toutes les données du formulaire de l'activity Recherche
        searchZone = getIntent().getStringExtra("searchZone");
        searchLocation = getIntent().getStringExtra("searchLocation");
        searchBc = getIntent().getStringExtra("searchBc") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchBc"));
        searchNo2 = getIntent().getStringExtra("searchNo2") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchNo2"));
        searchSo2 = getIntent().getStringExtra("searchSo2") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchSo2"));
        searchCo = getIntent().getStringExtra("searchCo") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchCo"));
        searchO3 = getIntent().getStringExtra("searchO3") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchO3"));
        searchPm25 = getIntent().getStringExtra("searchPm25") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchPm25"));
        searchPm10 = getIntent().getStringExtra("searchPm10") == null ? null : Double.parseDouble(getIntent().getStringExtra("searchPm10"));

        // Instanciation de l'adapter Location pour afficher la liste des locations recherchées
        mLocationAdapter = new LocationAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mLocationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBusManager.BUS.register(this);

        // Si l'activity n'était pas en background on récupère les données des locations à partir des API
        // Sinon on laisse l'affichage de l'activity comme elle était avant le onPause
        if(!background) {
            mProgressBar.setVisibility(View.VISIBLE);

            // Launch a search through the PlaceSearchService
            ZoneSearchService.INSTANCE.searchLocationFromParameter(this, searchZone, searchLocation, searchBc, searchNo2, searchSo2, searchO3, searchCo, searchPm25, searchPm10);
        }
        background = false; // FAUX car l'activity est visible par l'utilisateur
    }

    @Override
    protected void onPause() {
        EventBusManager.BUS.unregister(this);

        background = true; // VRAI car l'activity passe en background
        super.onPause();
    }

    // Méthode qui écoute l'événement SearchLocationEvent
    // Elle gère l'affichage des locations d'une zone
    @Subscribe
    public void searchResult(final SearchLocationsEvent event) {
        runOnUiThread(() -> {
            // Envoi des données reçus par le service à l'adapter Location pour qu'il affiche les locations recherchées
            mLocationAdapter.setLocations(event.getLocations());
            mLocationAdapter.notifyDataSetChanged();

            // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
            // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche un toast.
            // Si on a pas de données alors toast warning, sinon toast success
            if(UtilsService.INSTANCE.visibilityGone || event.getLocations().size() == 0) {
                mProgressBar.setVisibility(View.GONE);
                if(event.getLocations().size() == 0){
                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_warning);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "warning");
                } else {
                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success");
                }
            }
        });
    }
}