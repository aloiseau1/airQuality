package org.miage.airquality.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.miage.airquality.DetailLocationActivity;
import org.miage.airquality.R;
import org.miage.airquality.model.location.Location;
import org.miage.airquality.model.location.Parameter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

// Adapter permettant de compléter la liste des locations dans les vues ListeLocation, Favoris, RechercheListeLocation
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<Location> mLocations;

    public LocationAdapter(Activity context, List<Location> locations) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mLocations = locations;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.location_item, parent, false);
        LocationViewHolder holder = new LocationViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        // Chargement des données pour un éléments de la liste
        final Location location = mLocations.get(position);
        String city = location.getIdLocation() + " - " + location.getCity();

        String temperature = location.getTempActuelle();
        holder.mLocationCityTextView.setText(city);
        holder.mLocationTemperatureTextView.setText(temperature);
        cleanMeasurement(holder);
        for (Parameter parameter: location.getParameters()) {
            String stringParameter = parameter.getParameter();
            String stringMeasurement = location.getOneMeasurement(stringParameter);
            Double valueMeasurement = location.getValueOfOneMeasurement(stringParameter);
            switch (stringParameter){
                case "so2":
                    holder.mLocationSo2TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 20) {
                            holder.mLocationSo2TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 20) {
                            holder.mLocationSo2TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "no2":
                    holder.mLocationNo2TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 200) {
                            holder.mLocationNo2TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 200) {
                            holder.mLocationNo2TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "co":
                    holder.mLocationCoTextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 10) {
                            holder.mLocationCoTextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 10) {
                            holder.mLocationCoTextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "bc":
                    holder.mLocationBcTextView.setText(stringMeasurement);
                    break;
                case "o3":
                    holder.mLocationO3TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 100) {
                            holder.mLocationO3TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 100) {
                            holder.mLocationO3TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "pm10":
                    holder.mLocationPm10TextView.setText(stringMeasurement);
                    if(valueMeasurement != null) {
                        if (valueMeasurement > 40) {
                            holder.mLocationPm10TextView.setTextColor(Color.RED);
                        } else if (valueMeasurement <= 40) {
                            holder.mLocationPm10TextView.setTextColor(Color.parseColor("#096A09"));
                        }
                    }
                    break;
                case "pm25":
                    holder.mLocationPm25TextView.setText(stringMeasurement);
                    break;
                default:
                    break;
            }
        }

        // Implémentation d'un clickListener pour démarrer l'activité DetailLocation lors du clic sur la location
        holder.mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent detailZoneIntent = new Intent(context, DetailLocationActivity.class);
                detailZoneIntent.putExtra("location", location);
                context.startActivity(detailZoneIntent);
            }
        });
    }

    // Méthode permettant de mettre par défaut l'affichage des mesures d'une location
    private void cleanMeasurement(LocationViewHolder holder){
        holder.mLocationBcTextView.setText("bc : /   ");
        holder.mLocationBcTextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationPm25TextView.setText("pm25 : /   ");
        holder.mLocationPm25TextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationPm10TextView.setText("pm10 : /   ");
        holder.mLocationPm10TextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationO3TextView.setText("o3 : /   ");
        holder.mLocationO3TextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationCoTextView.setText("co : /   ");
        holder.mLocationCoTextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationNo2TextView.setText("no2 : /   ");
        holder.mLocationNo2TextView.setTextColor(Color.parseColor("#808080"));
        holder.mLocationSo2TextView.setText("so2 : /   ");
        holder.mLocationSo2TextView.setTextColor(Color.parseColor("#808080"));
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }

    public void setLocations(List<Location> locations) {
        this.mLocations = locations;
    }

    // Pattern ViewHolder
    class LocationViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.location_adapter_city)
        TextView mLocationCityTextView;

        @BindView(R.id.location_adapter_temperature)
        TextView mLocationTemperatureTextView;

        @BindView(R.id.location_adapter_no2)
        TextView mLocationNo2TextView;

        @BindView(R.id.location_adapter_so2)
        TextView mLocationSo2TextView;

        @BindView(R.id.location_adapter_co)
        TextView mLocationCoTextView;

        @BindView(R.id.location_adapter_bc)
        TextView mLocationBcTextView;

        @BindView(R.id.location_adapter_pm10)
        TextView mLocationPm10TextView;

        @BindView(R.id.location_adapter_pm25)
        TextView mLocationPm25TextView;

        @BindView(R.id.location_adapter_o3)
        TextView mLocationO3TextView;

        @BindView(R.id.location)
        LinearLayout mLocation;

        public LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
