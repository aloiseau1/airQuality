package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

// Activity principale
// Elle contient les actions lors du clic sur les boutons de la vue pour accéder aux différentes fonctionnalités de l'application
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.buttonListeZones)
    public void clickedOnSwitchToListeZones() {
        Intent switchToListeZonesIntent = new Intent(this, ListeZonesActivity.class);
        startActivity(switchToListeZonesIntent);
    }

    @OnClick(R.id.buttonCarteZones)
    public void clickedOnSwitchToCarteZones() {
        Intent switchToCarteZonesIntent = new Intent(this, CarteZonesActivity.class);
        startActivity(switchToCarteZonesIntent);
    }

    @OnClick(R.id.buttonRecherche)
    public void clickedOnSwitchToRecherche() {
        Intent switchToRechercheIntent = new Intent(this, RechercheActivity.class);
        startActivity(switchToRechercheIntent);
    }

    @OnClick(R.id.buttonFavoris)
    public void clickedOnSwitchToFavoris() {
        Intent switchToFavorisIntent = new Intent(this, FavorisActivity.class);
        startActivity(switchToFavorisIntent);
    }
}
