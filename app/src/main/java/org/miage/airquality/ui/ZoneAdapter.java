package org.miage.airquality.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.miage.airquality.ListeLocationsActivity;
import org.miage.airquality.R;
import org.miage.airquality.model.responsezone.Zone;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

// Adapter permettant de compléter la liste des zones dans la vue ListeZone
public class ZoneAdapter extends RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<Zone> mZone;

    public ZoneAdapter(Activity context, List<Zone> resultsZones) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mZone = resultsZones;
    }

    @Override
    public ZoneAdapter.ZoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.zone_item, parent, false);
        ZoneAdapter.ZoneViewHolder holder = new ZoneAdapter.ZoneViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ZoneAdapter.ZoneViewHolder holder, int position) {
        // Chargement des données pour un éléments de la liste
        final Zone resultsZone = mZone.get(position);
        holder.mZoneCityTextView.setText(resultsZone.getName());

        // Implémentation d'un clickListener pour démarrer l'activité ListeLocation lors du clic sur une zone
        holder.mZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent listLocationsIntent = new Intent(context, ListeLocationsActivity.class);
                listLocationsIntent.putExtra("name", resultsZone.getName());
                context.startActivity(listLocationsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mZone.size();
    }

    public void setZones(List<Zone> resultsZones) {
        this.mZone = resultsZones;
    }

    // Pattern ViewHolder
    class ZoneViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.zone_adapter_city)
        TextView mZoneCityTextView;

        @BindView(R.id.zone)
        LinearLayout mZone;

        public ZoneViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
