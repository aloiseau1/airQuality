package org.miage.airquality;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.otto.Subscribe;

import org.miage.airquality.event.EventBusManager;
import org.miage.airquality.event.locationevent.SearchLocationsEvent;
import org.miage.airquality.service.UtilsService;
import org.miage.airquality.service.ZoneSearchService;
import org.miage.airquality.ui.LocationAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Activity qui affiche les locations
public class ListeLocationsActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.activity_liste_locations_loader)
    ProgressBar mProgressBar;

    @BindView(R.id.location_search)
    EditText mLocationSearch;

    private LocationAdapter mLocationAdapter;
    private String city; // Variable qui contient la zone qui correspond aux locations qui sont affichées dans la vue
    private Boolean searchProgressBar = false; // Variable qui permet de savoir si on est sur une recherche direct en BDD, elle est utilisé pour afficher ou non la progressBar
    private Boolean background = false; // Variable qui permet de savoir si l'activité est en background (en onPause)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_locations);

        ButterKnife.bind(this);

        city = getIntent().getStringExtra("name"); // Récupération du nom de la zone envoyée par l'activity ListeZones

        // Instanciation de l'adapter Location pour afficher la liste des locations d'une zone
        mLocationAdapter = new LocationAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mLocationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader

        // Implémentation d'un textChangedListener sur l'editText qui permet de faire une recherche par nom sur les locations d'une zone
        mLocationSearch.addTextChangedListener(new TextWatcher() {
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
                mProgressBar.setVisibility(View.VISIBLE); // Affiche le loader
                searchProgressBar = true; // VRAI car on est sur une recherche direct en BDD

                // Lancement d'une recherche direct en BDD avec le nom de la location recherchée
                ZoneSearchService.INSTANCE.searchInBDDLocation(city, true, editable.toString(), false);
            }
        });
    }

    // Méthode qui est appelée lors du click sur l'icon info de la vue
    // Elle affiche une modal contenant les définitions des mesures
    @OnClick(R.id.ic_info)
    public void clickedInfo() {
        InfoDialogActivity myDialogFragment = new InfoDialogActivity();
        myDialogFragment.show(getSupportFragmentManager(),"alert");
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBusManager.BUS.register(this);

        // Si l'activity n'était pas en background on récupère les données des locations à partir des API
        // Sinon on laisse l'affichage de l'activity comme elle était avant le onPause
        if(!background) {
            mProgressBar.setVisibility(View.VISIBLE);

            ZoneSearchService.INSTANCE.searchLocationFromCity(this, "FR", city, mLocationSearch.getText().toString(), false);
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
        runOnUiThread (() -> {
            // Envoi des données reçus par le service à l'adapter Location pour qu'il affiche les locations d'une zone
            mLocationAdapter.setLocations(event.getLocations());
            mLocationAdapter.notifyDataSetChanged();

            // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
            // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche un toast.
            // Si on a pas de données alors toast warning, sinon toast success
            if(UtilsService.INSTANCE.visibilityGone || searchProgressBar || event.getLocations().size() == 0) {
                mProgressBar.setVisibility(View.GONE);
                if(event.getLocations().size() == 0){
                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_warning);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "warning");
                } else if(!searchProgressBar){
                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success");
                }
                searchProgressBar = false;
            }
        });
    }
}
