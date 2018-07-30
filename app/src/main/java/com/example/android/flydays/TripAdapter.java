package com.example.android.flydays;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TripAdapter extends ArrayAdapter<Trip> {

    public TripAdapter(Context context, List<Trip> trips) {
        super(context, 0, trips);
    }

    private static final String LOG_TAG = TripAdapter.class.getName();


    //for format representation: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    //method used later for date formatting
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd LLL");
        return dateFormat.format(dateObject);
    }

    //for local time the constructor can also have a locale variable:
    //SimpleDateFormat sdf = new SimpleDateFormat("MMM d H:mm", Locale.CANADA);
    //todo: add locale to my times as they now show summer time (+1 hour)
    // check this to implement https://stackoverflow.com/questions/6567923/timezone-conversion

    //method used later for time formatting
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(dateObject);
    }

    //method to calculate duration of one way trip from seconds
    private String secToDuration(long seconds){
        long hours = seconds/3600;
        long minutes = (seconds % 3600)/60;
        return hours + "h " + minutes + "m";
    }



        public View getView(int position, View convertView, ViewGroup parent){
            View listItemView = convertView;
            if (listItemView==null){
                listItemView = LayoutInflater.from(getContext()).inflate(
                        R.layout.trip_list_item, parent,false);
            }

            Trip currentTrip = getItem(position);


            TextView destinationView = (TextView) listItemView.findViewById(R.id.destination_city);
            destinationView.setText(currentTrip.getArrCity());

            //TextView destinationCountry = (TextView) listItemView.findViewById(R.id.destination_country);
            //destinationCountry.setText(", " + currentTrip.getArrCountry());

            String destType = currentTrip.getDestType();
            //Log.e(LOG_TAG, "destType type" + destType);


            if(destType.equals("city") || destType.equals("airport")){
                destinationView.setVisibility(View.GONE);
            }


            TextView priceView = (TextView) listItemView.findViewById(R.id.trip_price);
            priceView.setText("£"+String.valueOf(currentTrip.getPrice()));

            TextView depDurationView = (TextView) listItemView.findViewById(R.id.out_duration);
            depDurationView.setText(secToDuration(currentTrip.getDepDuration()));

            TextView retDurationView = (TextView) listItemView.findViewById(R.id.ret_duration);
            if(currentTrip.getRetDuration() != 0)
                retDurationView.setText(secToDuration(currentTrip.getRetDuration()));

            List<Flight> flights = currentTrip.getFlights();

            //assuming we are working with direct flights only
            //variables need to be declared
            Flight outbound = null;
            Flight returnn = null;

            if(flights.size()>=2) {
                outbound = flights.get(0);
                returnn = flights.get(1);
            }else if (flights.size()==1) {
                outbound = flights.get(0);
            }else {
                outbound = null;
            }

            if(outbound != null) {
                TextView outLogoView = (TextView) listItemView.findViewById(R.id.out_airline);

                outLogoView.setText(String.valueOf(outbound.getAirlineCode()));

                TextView outDepAirportView = (TextView) listItemView.findViewById(R.id.out_dep_airport_code);
                outDepAirportView.setText(outbound.getDepAirportCode());

                TextView outArrAirportView = (TextView) listItemView.findViewById(R.id.out_arr_airport_code);
                outArrAirportView.setText(outbound.getArrAirportCode());

                //there must be *1000L as the time is in seconds and java works with milliseconds
                //using - 3600000 as equivalent to 1 hour difference between our time and CZ?
                //todo: check the times, now it shows hour less
                Date outDeparture = new Date(outbound.getDepTimeInSeconds()*1000L - 3600000);
                Date outArrival = new Date(outbound.getArrTimeInSeconds()*1000L - 3600000);


                TextView outDepTimeView = (TextView) listItemView.findViewById(R.id.out_dep_time);
                String outDepTime = formatTime(outDeparture);
                outDepTimeView.setText(outDepTime);

                TextView outArrTimeView = (TextView) listItemView.findViewById(R.id.out_arr_time);
                String outArrTime = formatTime(outArrival);
                outArrTimeView.setText(outArrTime);

                TextView depDateView = (TextView) listItemView.findViewById(R.id.out_departure_date);
                String depDate = formatDate(outDeparture);
                depDateView.setText(depDate);

            }

            if(returnn != null) {
                TextView retLogoView = (TextView) listItemView.findViewById(R.id.ret_airline);
                retLogoView.setText(String.valueOf(returnn.getAirlineCode()));

                TextView retDepAirportView = (TextView) listItemView.findViewById(R.id.ret_dep_airport_code);
                retDepAirportView.setText(returnn.getDepAirportCode());

                TextView retArrAirportView = (TextView) listItemView.findViewById(R.id.ret_arr_airport_code);
                retArrAirportView.setText(returnn.getArrAirportCode());

                Date retDeparture = new Date(returnn.getDepTimeInSeconds()*1000L - 3600000);
                Date retArrival = new Date(returnn.getArrTimeInSeconds()*1000L - 3600000);

                TextView retDepTimeView = (TextView) listItemView.findViewById(R.id.ret_dep_time);
                String retDepTime = formatTime(retDeparture);
                retDepTimeView.setText(retDepTime);

                TextView retArrTimeView = (TextView) listItemView.findViewById(R.id.ret_arr_time);
                String retArrTime = formatTime(retArrival);
                retArrTimeView.setText(retArrTime);

                TextView retDateView = (TextView) listItemView.findViewById(R.id.ret_departure_date);
                String retDate = formatDate(retDeparture);
                retDateView.setText(retDate);
            }else {
                //if it's one way flight, hide the second part of the xml layout
                View layout = listItemView.findViewById(R.id.return_flight);
                layout.setVisibility(View.GONE);
            }

            return listItemView;
        }


}


