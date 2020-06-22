package org.miage.airquality.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.miage.airquality.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Service qui regroupe plusieurs méthodes et attributs qui sont utilisés dans les services et activity
public class UtilsService {

    public static UtilsService INSTANCE = new UtilsService();
    private static Context context; // Contient le contexte en cours lorsque le service est appelé
    public Boolean visibilityGone; // Booléen permettant de savoir si la progressBar de chargement doit être visible ou non

    // Récupération du contexte à chaque fois que l'on récupère l'instance du service
    public static UtilsService getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return INSTANCE;
    }

    // Méthode permettant de savoir si une chaîne de caractère est contenu dans un tableau de chaîne de caractère
    public Boolean searchValueInArray(List<String> tableau, String value){
        Boolean inArray = false;
        for(String element: tableau){
            if(element.equals(value)){
                inArray = true;
            }
        }
        return inArray;
    }

    // Permet de transformer une date au format YYYY-MM-dd à un format littérraire (ex: lundi 3 avril 2020)
    public String getDate(String stringDate){
        String[] dateExplode = stringDate.split("-");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dateExplode[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateExplode[1])-1);
        calendar.set(Calendar.DATE, Integer.parseInt(dateExplode[2]));
        Date dateTime = calendar.getTime();
        return dateFormat.format(dateTime);
    }

    // Méthode qui permet de savoir si le téléphone est connecté à internet ou non
    public Boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();


        } catch (Exception e) {
            Log.v("connectivity", e.toString());
            return false;
        }
    }

    // Méthode qui permet d'afficher un toast à l'écran
    public void showToast(LayoutInflater inflater, ViewGroup viewGroup, String typeToast){
        View layout;
        switch (typeToast){
            case "success":
                layout = inflater.inflate(R.layout.toast_success, viewGroup);
                break;
            case "success_add_favoris":
                layout = inflater.inflate(R.layout.toast_success_add_favoris, viewGroup);
                break;
            case "success_remove_favoris":
                layout = inflater.inflate(R.layout.toast_success_remove_favoris, viewGroup);
                break;
            case "warning":
                layout = inflater.inflate(R.layout.toast_warning, viewGroup);
                break;
            case "error":
                layout = inflater.inflate(R.layout.toast_error, viewGroup);
                break;
            default:
                layout = inflater.inflate(R.layout.toast_error, viewGroup);
                break;
        }

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        if(typeToast.equals("error")){
            toast.setDuration(Toast.LENGTH_LONG);
        } else {
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.setView(layout);
        toast.show();
    }

    // Méthode sui permet de récupérer la date actuelle au format texte (ex: lundi 3 avril 2020)
    public String getDateActuelle(){
        Date actuelle = new Date();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
        return dateFormat.format((actuelle));
    }
}
