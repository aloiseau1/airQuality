package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

// Activity qui affiche les locations favorites
public class FavorisActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_favoris_loader)
    ProgressBar mProgressBar;
    @BindView(R.id.favoris_search)
    EditText mLocationFavorisSearch;

    private LocationAdapter mLocationAdapter;
    private Boolean searchProgressBar = false; // Variable qui permet de savoir si on est sur une recherche direct en BDD, elle est utilisé pour afficher ou non la progressBar
    private Boolean background = false; // Variable qui permet de savoir si l'activité est en background (en onPause)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoris);

        ButterKnife.bind(this);

        // Instanciation de l'adapter Location pour afficher la liste des locations favorites
        mLocationAdapter = new LocationAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mLocationAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Implémentation d'un textChangedListener sur l'editText qui permet de faire une recherche par nom sur les locations favorites
        mLocationFavorisSearch.addTextChangedListener(new TextWatcher() {
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
                ZoneSearchService.INSTANCE.searchInBDDLocationByFavoris(true,editable.toString());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        EventBusManager.BUS.register(this);

        // Si l'activity n'était pas en background on récupère les données des locations à partir des API
        // Sinon on récupère les données en BDD
        if(!background) {
            mProgressBar.setVisibility(View.VISIBLE);

            ZoneSearchService.INSTANCE.searchLocationFromFavoris(this, mLocationFavorisSearch.getText().toString());
        } else {
            ZoneSearchService.INSTANCE.searchInBDDLocationByFavoris(true,mLocationFavorisSearch.getText().toString());
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
    // Elle gère l'affichage des locations favorites
    @Subscribe
    public void searchResult(final SearchLocationsEvent event) {
        runOnUiThread(() -> {
            // Envoi des données reçus par le service à l'adapter Location pour qu'il affiche les locations
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