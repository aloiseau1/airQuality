package org.miage.airquality.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.miage.airquality.R;
import org.miage.airquality.model.location.DayPrevision;
import org.miage.airquality.service.UtilsService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

// Adapter permettant de compléter la liste des jours de prévisions dans la vue DétailLocation
public class DayPrevisionAdapter extends RecyclerView.Adapter<DayPrevisionAdapter.DayPrevisionViewHolder>{

    private LayoutInflater inflater;
    private Activity context;
    private List<DayPrevision> mDayPrevisions;

    public DayPrevisionAdapter(Activity context, List<DayPrevision> dayPrevisions) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.mDayPrevisions = dayPrevisions;
    }

    @Override
    public DayPrevisionAdapter.DayPrevisionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.day_prevision_item, parent, false);
        DayPrevisionAdapter.DayPrevisionViewHolder holder = new DayPrevisionAdapter.DayPrevisionViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DayPrevisionAdapter.DayPrevisionViewHolder holder, int position) {
        // Chargement des données pour un éléments de la liste
        final DayPrevision dayPrevision = mDayPrevisions.get(position);

        String conditionMatin = dayPrevision.getConditionMatin() + " ";
        String conditionApresMidi = dayPrevision.getConditionApresMidi() + " ";
        String pluieMatin = "Pluie : " + dayPrevision.getStringPluieMatin() + " mm";
        String pluieApresMidi = "Pluie : " + dayPrevision.getStringPluieApresMidi() + " mm";

        setImageView(holder, dayPrevision.getConditionMatin(), "matin");
        setImageView(holder, dayPrevision.getConditionApresMidi(), "après midi");

        holder.mDetailJourDateTextView.setText(UtilsService.INSTANCE.getDate(dayPrevision.getDate()));

        holder.mDetailJourConditionMatinTextView.setText(conditionMatin);
        holder.mDetailJourTempMinMatinTextView.setText(getTemp(dayPrevision.getStringTMinMatin(), "Min"));
        holder.mDetailJourTempMaxMatinTextView.setText(getTemp(dayPrevision.getStringTMaxMatin(), "Max"));
        holder.mDetailJourPluieMatinTextView.setText(pluieMatin);

        holder.mDetailJourConditionApresMidiTextView.setText(conditionApresMidi);
        holder.mDetailJourTempMinApresMidiTextView.setText(getTemp(dayPrevision.getStringTMinApresMidi(), "Min"));
        holder.mDetailJourTempMaxApresMidiTextView.setText(getTemp(dayPrevision.getStringTMaxApresMidi(), "Max"));
        holder.mDetailJourPluieApresMidiTextView.setText(pluieApresMidi);
    }

    @Override
    public int getItemCount() {
        return mDayPrevisions.size();
    }

    // Méthode qui peremt d'afficher les images des conditions météos en fontion du paramètre condition
    private void setImageView(DayPrevisionAdapter.DayPrevisionViewHolder holder, String condition, String time){
        switch (condition){
            case "Ensoleillé":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_sunny);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_sunny);
                }
                break;
            case "Peu nuageux":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_peu_nuageux);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_peu_nuageux);
                }
                break;
            case "Nuageux":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_cloudy);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_cloudy);
                }
                break;
            case "Très nuageux":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_to_cloudy);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_to_cloudy);
                }
                break;
            case "Faible risque de pluie":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_risk_of_rain);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_risk_of_rain);
                }
                break;
            case "Pluie":
                if(time.equals("matin")){
                    holder.mIcMatin.setImageResource(R.drawable.ic_rain);
                } else {
                    holder.mIcApresMidi.setImageResource(R.drawable.ic_rain);
                }
                break;
            default:
                break;
        }
    }

    // Méthode qui retourne le contenu affiché pour les temperatures
    private String getTemp(String temp, String type){
        if(temp != null){
            return type + ": " + temp + "°";
        } else {
            return type + ": /";
        }
    }

    // Pattern ViewHolder
    class DayPrevisionViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.detail_jour_date)
        TextView mDetailJourDateTextView;

        @BindView(R.id.detail_jour_condition_matin)
        TextView mDetailJourConditionMatinTextView;

        @BindView(R.id.detail_jour_temp_min_matin)
        TextView mDetailJourTempMinMatinTextView;

        @BindView(R.id.detail_jour_temp_max_matin)
        TextView mDetailJourTempMaxMatinTextView;

        @BindView(R.id.detail_jour_pluie_matin)
        TextView mDetailJourPluieMatinTextView;

        @BindView(R.id.detail_jour_condition_apres_midi)
        TextView mDetailJourConditionApresMidiTextView;

        @BindView(R.id.detail_jour_temp_min_apres_midi)
        TextView mDetailJourTempMinApresMidiTextView;

        @BindView(R.id.detail_jour_temp_max_apres_midi)
        TextView mDetailJourTempMaxApresMidiTextView;

        @BindView(R.id.detail_jour_pluie_apres_midi)
        TextView mDetailJourPluieApresMidiTextView;

        @BindView(R.id.ic_matin)
        ImageView mIcMatin;

        @BindView(R.id.ic_apres_midi)
        ImageView mIcApresMidi;

        public DayPrevisionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
