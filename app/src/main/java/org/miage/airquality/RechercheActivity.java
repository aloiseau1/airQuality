package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Activity qui affiche le formulaire de recherche sur les locations déjà visitées
public class RechercheActivity extends AppCompatActivity {

    @BindView(R.id.recherche_bc)
    EditText mRechercheBc;
    @BindView(R.id.recherche_co)
    EditText mRechercheCo;
    @BindView(R.id.recherche_location)
    EditText mRechercheLocation;
    @BindView(R.id.recherche_no2)
    EditText mRechercheNo2;
    @BindView(R.id.recherche_o3)
    EditText mRechercheO3;
    @BindView(R.id.recherche_pm10)
    EditText mRecherchePm10;
    @BindView(R.id.recherche_pm25)
    EditText mRecherchePm25;
    @BindView(R.id.recherche_so2)
    EditText mRechercheSo2;
    @BindView(R.id.recherche_zone)
    EditText mRechercheZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche);

        ButterKnife.bind(this);
    }

    // Méthode qui est appelée lors du clic sur le bouton de recherche
    // Elle récupère toutes les données saisies par l'utilisateur et elle les envoie à l'activity RechercheListeLocation
    @OnClick(R.id.button_lancement_recherche)
    public void clickedOnSearchLocationWithParamater() {
        String searchZone = mRechercheZone.getText().toString().equals("") ? null : mRechercheZone.getText().toString();
        String searchLocation = mRechercheLocation.getText().toString().equals("") ? null : mRechercheLocation.getText().toString();
        String searchBc = mRechercheBc.getText().toString().equals("") ? null : mRechercheBc.getText().toString().replace(",",".");
        String searchNo2 = mRechercheNo2.getText().toString().equals("") ? null : mRechercheNo2.getText().toString().replace(",",".");
        String searchSo2 = mRechercheSo2.getText().toString().equals("") ? null : mRechercheSo2.getText().toString().replace(",",".");
        String searchCo = mRechercheCo.getText().toString().equals("") ? null : mRechercheCo.getText().toString().replace(",",".");
        String searchO3 = mRechercheO3.getText().toString().equals("") ? null : mRechercheO3.getText().toString().replace(",",".");
        String searchPm10 = mRecherchePm10.getText().toString().equals("") ? null : mRecherchePm10.getText().toString().replace(",",".");
        String searchPm25 = mRecherchePm25.getText().toString().equals("") ? null : mRecherchePm25.getText().toString().replace(",",".");

        Intent rechercheListeLocationIntent = new Intent(this, RechercheListeLocationActivity.class);
        rechercheListeLocationIntent.putExtra("searchZone", searchZone);
        rechercheListeLocationIntent.putExtra("searchLocation", searchLocation);
        rechercheListeLocationIntent.putExtra("searchBc", searchBc);
        rechercheListeLocationIntent.putExtra("searchNo2", searchNo2);
        rechercheListeLocationIntent.putExtra("searchSo2", searchSo2);
        rechercheListeLocationIntent.putExtra("searchCo", searchCo);
        rechercheListeLocationIntent.putExtra("searchO3", searchO3);
        rechercheListeLocationIntent.putExtra("searchPm10", searchPm10);
        rechercheListeLocationIntent.putExtra("searchPm25", searchPm25);
        this.startActivity(rechercheListeLocationIntent);
    }
}
