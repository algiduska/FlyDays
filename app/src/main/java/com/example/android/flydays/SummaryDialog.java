package com.example.android.flydays;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SummaryDialog extends AppCompatDialogFragment {

    private static final String LOG_TAG = SummaryDialog.class.getName();
    Context context;

    private String airlineO;
    private String airlineR;
    private String airlineCodeO;
    private String airlineCodeR;
    private TextView airlineV;
    private TextView airline2V;
    private ImageView pic;
    private ImageView airlineLogo;
    private ImageView airlineLogo2;

    private String imageCityUrl;
    private String airlineUrlO;
    private String airlineUrlR;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        context = getContext();

        //how to do in a fragment: Sam - https://stackoverflow.com/questions/15459209/passing-argument-to-dialogfragment
        Bundle bundle = getArguments();

        String uri = bundle.getString("uri");
        final Uri bookingUri = Uri.parse(uri);
        //the rest in onClick method

        String depCity = bundle.getString("depc");
        String holCity = bundle.getString("holc");
        String country = bundle.getString("country");
        int price = bundle.getInt("price");
        imageCityUrl = bundle.getString("cityurl");

        String airFromOut = bundle.getString("airFO");
        String airToOut = bundle.getString("airTO");
        airlineCodeO = bundle.getString("airlineCodeO");
        airlineO = bundle.getString("airlineO");
        Log.e(LOG_TAG, "Airline outgoing as received through the bundle: " + airlineO);

        String durO = bundle.getString("durO");
        String timeDepOut = bundle.getString("timeDO");
        String timeArrOut = bundle.getString("timeAO");
        String dateDepOut = bundle.getString("dateDO");
        String dateArrOut = bundle.getString("dateAO");

        String airFromRet= "";
        String airToRet = "";
        airlineR = "";
        String durR = "";
        String timeDepRet ="";
        String timeArrRet= "";
        String dateDepRet ="";
        String dateArrRet ="";

        airlineUrlO = "https://images.kiwi.com/airlines/64/" + airlineCodeO + ".png";
        Log.e(LOG_TAG, "Airline outgoing url: " + airlineUrlO);


        boolean returnn= bundle.getBoolean("return");


        if (returnn){
            airFromRet = bundle.getString("airFR");
            airToRet = bundle.getString("airTR");
            airlineR = bundle.getString("airlineR");
            Log.e(LOG_TAG, "Airline return as received through the bundle: " + airlineR);
            airlineCodeR = bundle.getString("airlineCodeR");
            durR = bundle.getString("durR");
            timeDepRet = bundle.getString("timeDR");
            timeArrRet = bundle.getString("timeAR");
            dateDepRet = bundle.getString("dateDR");
            dateArrRet = bundle.getString("dateAR");

            airlineUrlR = "https://images.kiwi.com/airlines/64/" + airlineCodeR + ".png";
            Log.e(LOG_TAG, "Airline return url: " + airlineUrlR);
        }



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.summary_dialog, null);

        //views with pictures on it (one more in returnn)
        pic = view.findViewById(R.id.city_pic);
        Picasso
                .with(context)
                .load(imageCityUrl)
                .fit()
                .into(pic);

        airlineLogo = view.findViewById(R.id.airline_logo);
        Picasso
                .with(context)
                .load(airlineUrlO)
                .fit()
                .into(airlineLogo);


        //all other views for outgoing flight
        TextView desCityV = view.findViewById(R.id.city);
        desCityV.setText(holCity);
        TextView countryV = view.findViewById(R.id.country);
        countryV.setText(country);
        TextView priceV = view.findViewById(R.id.price_total);
        priceV.setText("£" + String.valueOf(price));

        TextView fromCityV = view.findViewById(R.id.from_city);
        fromCityV.setText(depCity);
        TextView fromAirV = view.findViewById(R.id.from_airport);
        fromAirV.setText(airFromOut);

        TextView timeoutV = view.findViewById(R.id.time_out);
        timeoutV.setText(timeDepOut);
        TextView timearrV = view.findViewById(R.id.time_arr);
        timearrV.setText(timeArrOut);
        TextView dateoutV = view.findViewById(R.id.date_out);
        dateoutV.setText(dateDepOut);
        TextView datearrV = view.findViewById(R.id.date_arr);
        datearrV.setText(dateArrOut);
        Log.e(LOG_TAG, "Date arr outgoing: " + dateArrOut);

        TextView durV = view.findViewById(R.id.duration);
        durV.setText(durO);
        airlineV = view.findViewById(R.id.airline);
        airlineV.setText(airlineO);

        TextView toCityV = view.findViewById(R.id.to_city);
        toCityV.setText(holCity);
        TextView toAirV = view.findViewById(R.id.to_airport);
        toAirV.setText(airToOut);

        //views holding return flight information
        View returnFlightV = view.findViewById(R.id.flight2);

        if(returnn){
            airlineLogo2 = view.findViewById(R.id.airline_logo2);
            Picasso
                    .with(context)
                    .load(airlineUrlR)
                    .fit()
                    .into(airlineLogo2);

            TextView fromCity2V = view.findViewById(R.id.from_city2);
            fromCity2V.setText(holCity);
            TextView fromAir2V = view.findViewById(R.id.from_airport2);
            fromAir2V.setText(airFromRet);

            TextView timeout2V = view.findViewById(R.id.time_out2);
            timeout2V.setText(timeDepRet);
            TextView timearr2V = view.findViewById(R.id.time_arr2);
            timearr2V.setText(timeArrRet);
            TextView dateout2V = view.findViewById(R.id.date_out2);
            dateout2V.setText(dateDepRet);
            TextView datearr2V = view.findViewById(R.id.date_arr2);
            datearr2V.setText(dateArrRet);
            Log.e(LOG_TAG, "Date arr returning: " + dateArrRet);

            TextView dur2V = view.findViewById(R.id.duration2);
            dur2V.setText(durR);
            airline2V = view.findViewById(R.id.airline2);
            airline2V.setText(airlineR);

            TextView toCity2V = view.findViewById(R.id.to_city2);
            toCity2V.setText(depCity);
            TextView toAir2V = view.findViewById(R.id.to_airport2);
            toAir2V.setText(airToRet);
        }else{
            returnFlightV.setVisibility(View.GONE);
        }


        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Book", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookingUri);
                        startActivity(websiteIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SummaryDialog.this.getDialog().cancel();
                    }
                });




        return builder.create();
    }

}
