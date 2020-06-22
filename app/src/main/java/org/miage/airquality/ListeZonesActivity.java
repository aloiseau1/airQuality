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
import org.miage.airquality.event.zoneevent.SearchZonesEvent;
import org.miage.airquality.service.UtilsService;
import org.miage.airquality.service.ZoneSearchService;
import org.miage.airquality.ui.ZoneAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

// Activity qui affiche les zones
public class ListeZonesActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.activity_liste_zones_loader)
    ProgressBar mProgressBar;

    @BindView(R.id.zone_search)
    EditText mZoneSearch;

    private ZoneAdapter mZoneAdapter;
    private Boolean searchProgressBar = false; // Variable qui permet de savoir si on est sur une recherche direct en BDD, elle est utilisé pour afficher ou non la progressBar
    private Boolean background = false; // Variable qui permet de savoir si l'activité est en background (en onPause)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_zones);

        ButterKnife.bind(this);

        // Instanciation de l'adapter Zone pour afficher la liste des zones
        mZoneAdapter = new ZoneAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(mZoneAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader

        // Implémentation d'un textChangedListener sur l'editText qui permet de faire une recherche par nom sur les zones
        mZoneSearch.addTextChangedListener(new TextWatcher() {
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
                mProgressBar.setVisibility(View.VISIBLE); // Affichage du loader
                searchProgressBar = true; // VRAI car on est sur une recherche direct en BDD

                // Lancement d'une recherche direct en BDD avec le nom de la zone recherchée
                ZoneSearchService.INSTANCE.searchInBDDZone(true, editable.toString(), false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBusManager.BUS.register(this);

        // Si l'activity n'était pas en background on récupère les données des zones à partir des API
        // Sinon on laisse l'affichage de l'activity comme elle était avant le onPause
        if(!background) {
            mProgressBar.setVisibility(View.VISIBLE);

            ZoneSearchService.INSTANCE.searchZoneFromCountry(this, "FR", 10000, mZoneSearch.getText().toString(), false);
        }
        background = false; // FAUX car l'activity est visible par l'utilisateur
    }

    @Override
    protected void onPause() {
        EventBusManager.BUS.unregister(this);

        background = true; // VRAI car l'activity passe en background
        super.onPause();
    }

    // Méthode qui écoute l'événement SearchZoneEvent
    // Elle gère l'affichage des zones
    @Subscribe
    public void searchResult(final SearchZonesEvent event) {
        runOnUiThread (() -> {
            // Envoi des données reçus par le service à l'adapter Zone pour qu'il affiche les zones
            mZoneAdapter.setZones(event.getResultsZones());
            mZoneAdapter.notifyDataSetChanged();

            // Si le chargement des données est terminées avec l'ensemble des appels API terminé,
            // ou la recherche direct en BDD est terminée alors on enlève le loader et on affiche un toast.
            // Si on a pas de données alors toast warning, sinon toast success
            if(UtilsService.INSTANCE.visibilityGone || searchProgressBar || event.getResultsZones().size() == 0) {
                mProgressBar.setVisibility(View.GONE);
                if(event.getResultsZones().size() == 0){
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
