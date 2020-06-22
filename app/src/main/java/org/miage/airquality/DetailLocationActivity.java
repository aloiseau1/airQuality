package org.miage.airquality;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.miage.airquality.model.location.Location;
import org.miage.airquality.model.location.Parameter;
import org.miage.airquality.service.UtilsService;
import org.miage.airquality.service.ZoneSearchService;
import org.miage.airquality.ui.DayPrevisionAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Activity qui affiche les détails d'une location
public class DetailLocationActivity extends AppCompatActivity {

    @BindView(R.id.detail_city)
    TextView mDetailCityTextView;

    @BindView(R.id.detail_temperature)
    TextView mDetailTemperatureTextView;

    @BindView(R.id.detail_date)
    TextView mDetailDateTextView;

    @BindView(R.id.detail_bc)
    TextView mDetailBcTextView;

    @BindView(R.id.detail_co)
    TextView mDetailCoTextView;

    @BindView(R.id.detail_no2)
    TextView mDetailNo2TextView;

    @BindView(R.id.detail_o3)
    TextView mDetailO3TextView;

    @BindView(R.id.detail_pm10)
    TextView mDetailPm10TextView;

    @BindView(R.id.detail_pm25)
    TextView mDetailPm25TextView;

    @BindView(R.id.detail_so2)
    TextView mDetailSo2TextView;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.detail_street_view)
    ImageView mGoogleStreetView;

    @BindView(R.id.detail_favoris)
    ImageView mDetailFavoris;

    private static String key = "XXX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_zone);

        ButterKnife.bind(this);

        Date actuelle = new Date();
        DateFormat dateFormatHour = new SimpleDateFormat("HH");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String heureActuelle = dateFormatHour.format(actuelle); // Récupération de l'heure actuelle pour savoir si on est le matin ou l'après-midi
        String todayDate = dateFormat.format(actuelle); // Récupération de la date du jour pour récupérer les prévisions météos liées à ce jour

        // Récupération de la location envoyé par l'activity précédente
        Location location = (Location)getIntent().getSerializableExtra("location");

        // Instanciation de l'adapter DayPrevision pour afficher les jours de prévisions
        DayPrevisionAdapter mDayPrevisionAdapter;
        mDayPrevisionAdapter = new DayPrevisionAdapter(this, location.getTodayDayPrevisions(todayDate));
        mRecyclerView.setAdapter(mDayPrevisionAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Si aucun jour de prévision alors il y a une erreur de chargement de donnée (problème internet ou pas de donnée reçu par l'API météo)
        if(location.getTodayDayPrevisions(todayDate).size() == 0){
            LayoutInflater inflater = getLayoutInflater();
            ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_error);
            UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "error");
        }

        // Appel à l'API google pour récupérer l'image Google Street View à partir des coordonnées de la location
        String urlGoogleStreetView = "https://maps.googleapis.com/maps/api/streetview?size=1200x600&location=" + location.getCoordinates().getLatitude() + "," + location.getCoordinates().getLongitude() + "&heading=151.78&pitch=-0.76&key=" + key;
        Picasso.get().load(urlGoogleStreetView).into(mGoogleStreetView);

        // Affichage de la température actuelle avec la condition actuelle en fonction de si on se trouve le matin ou l'après-midi
        String temperature = "";
        if(location.getTodayDayPrevisions(todayDate).size() > 0) {
            if (Integer.parseInt(heureActuelle) >= 12) {
                temperature = location.getTodayDayPrevisions(todayDate).get(0).getConditionApresMidi() + " " +
                        location.getTodayDayPrevisions(todayDate).get(0).getStringTempActuelle();
            } else {
                temperature = location.getTodayDayPrevisions(todayDate).get(0).getConditionMatin() + " " +
                        location.getTodayDayPrevisions(todayDate).get(0).getStringTempActuelle();
            }
        }
        mDetailTemperatureTextView.setText(temperature);

        setMeasurment(location); // Affichage des mesures

        // Affichage de l'image qui permet de gérer si la location est favorite
        if(location.getFavoris()){
            mDetailFavoris.setImageResource(R.drawable.ic_star_favoris_full);
        } else {
            mDetailFavoris.setImageResource(R.drawable.ic_star_favoris_empty);
        }

        // Affichage de la ville de la location et de la date du jour
        String city = location.getCity();
        String date = UtilsService.getInstance(getApplicationContext()).getDateActuelle();
        mDetailCityTextView.setText(city);
        mDetailDateTextView.setText(date);

        // Implémentation d'un OnClickListener sur l'étoile de la vue pour gérer la fonctionnalité Favoris
        mDetailFavoris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location locationSave = ZoneSearchService.INSTANCE.searchInBDDLocationByIdLocation(location.getIdLocation());
                if(locationSave.getFavoris()){
                    locationSave.setFavoris(false);
                    mDetailFavoris.setImageResource(R.drawable.ic_star_favoris_empty);

                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success_remove_favoris);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success_remove_favoris");
                } else {
                    locationSave.setFavoris(true);
                    mDetailFavoris.setImageResource(R.drawable.ic_star_favoris_full);

                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup viewGroup = (ViewGroup) findViewById(R.id.custom_toast_container_success_add_favoris);
                    UtilsService.getInstance(getApplicationContext()).showToast(inflater, viewGroup, "success_add_favoris");
                }
                locationSave.save();
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

    // Méthode qui permet d'afficher les mesures
    private void setMeasurment(Location location){
        for (Parameter parameter: location.getParameters()) {
            String stringParameter = parameter.getParameter();
            String stringMeasurement = location.getOneMeasurement(stringParameter);
            Double valueMeasurement = location.getValueOfOneMeasurement(stringParameter);
            switch (stringParameter){
                case "so2":
                    mDetailSo2TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 20) {
                            mDetailSo2TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 20) {
                            mDetailSo2TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "no2":
                    mDetailNo2TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 200) {
                            mDetailNo2TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 200) {
                            mDetailNo2TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "co":
                    mDetailCoTextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 10) {
                            mDetailCoTextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 10) {
                            mDetailCoTextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "bc":
                    mDetailBcTextView.setText(stringMeasurement);
                    break;
                case "o3":
                    mDetailO3TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 100) {
                            mDetailO3TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 100) {
                            mDetailO3TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "pm10":
                    mDetailPm10TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 40) {
                            mDetailPm10TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 40) {
                            mDetailPm10TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "pm25":
                    mDetailPm25TextView.setText(stringMeasurement);
                    break;
                default:
                    break;
            }
        }
    }
}
