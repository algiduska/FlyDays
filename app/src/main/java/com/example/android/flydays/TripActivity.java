package com.example.android.flydays;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class TripActivity extends AppCompatActivity implements LoaderCallbacks<List<Trip>> {

//todo: PROBLEM with API calls for 'anywhere' as only the cheapest flights are listed for each destination which means that when I filter them by time there are not many left

    /** base URL for trip data from the Kiwi dataset */
    private static final String FLIGHT_REQUEST_URL =
            "https://api.skypicker.com/flights";

    /** Adapter for the list of trips */
    private TripAdapter tAdapter;

    /**
     * Constant value for the trip loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static int trip_loader_id;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    private static final String LOG_TAG = TripActivity.class.getName();

    private String depLoc;
    private String holLoc;
    private String depLocName;
    private String holLocName;
    private String fromDate;
    private String toDate;
    private boolean dirOnly;
    private boolean oneWay;
    private String langLoc;
    private String depString;
    private String retString;
    private ArrayList<String> depDates = new ArrayList<>();
    private ArrayList<String> retDates = new ArrayList<>();

    private String outDepMin;
    private String outDepMax;
    private String outArrMin;
    private String outArrMax;
    private String retDepMin;
    private String retDepMax;
    private String retArrMin;
    private String retArrMax;

    private TextView cityToCity;
    private TextView dateToDate;

    private String depCity;
    private String holCity;
    private String toCountry;
    private String airFromOutC;
    private String airToOutC;
    private String airFromRetC;
    private String airToRetC;
    private String airlineOutC;
    private String airlineBackC;
    private String durationOut;
    private String durationRet;
    private String depTimeOut;
    private String arrTimeOut;
    private String depTimeRet;
    private String arrTimeRet;
    private String depDateOut;
    private String arrDateOut;
    private String depDateRet;
    private String arrDateRet;

    private Trip currentTrip;

    public Trip getCurrentTrip(){
        return this.currentTrip;
    }



    //some methods needed for dialog
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        return dateFormat.format(dateObject);
    }

    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(dateObject);
    }

    private String secToDuration(long seconds){
        long hours = seconds/3600;
        long minutes = (seconds % 3600)/60;
        return hours + "h " + minutes + "m";
    }


        /**
     * Creates the url based on established preferences and uses it for API call to update
     * the search results
     */
    @Override
    public Loader<List<Trip>> onCreateLoader(int i, Bundle bundle) {
        Log.e(LOG_TAG, "Loader started");

        Uri baseUri = Uri.parse(FLIGHT_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        String onewayOnly = oneWay ? "oneway" : "round";
        String directOrNot = dirOnly ? "1" : "0";
        /*
        basic example:
        https://api.skypicker.com/flights?flyFrom=LON&to=anywhere&dateFrom=13/08/2018&dateTo=13/08/2018&
        returnFrom=15/08/2018&returnTo=15/08/2018&maxFlyDuration=3&typeFlight=round&directFlights=1&
        locale=en&partner=picky&price_to=100&limit=30&sort=price&asc=1
         */


        //todo: if there isn't return it would throw nullpointer exception --> fix
        uriBuilder.appendQueryParameter("flyFrom", depLoc);
        uriBuilder.appendQueryParameter("to", holLoc);
        uriBuilder.appendQueryParameter("dateFrom", depDates.get(trip_loader_id)); //use triploaderID here?
        uriBuilder.appendQueryParameter("dateTo", depDates.get(trip_loader_id));
        uriBuilder.appendQueryParameter("returnFrom", retDates.get(trip_loader_id));
        uriBuilder.appendQueryParameter("returnTo",retDates.get(trip_loader_id));
        uriBuilder.appendQueryParameter("typeFlight", onewayOnly);
        uriBuilder.appendQueryParameter("directFlights", directOrNot);
        uriBuilder.appendQueryParameter("locale", langLoc);
        uriBuilder.appendQueryParameter("dtimefrom", outDepMin);
        uriBuilder.appendQueryParameter("dtimeto", outDepMax);
        uriBuilder.appendQueryParameter("atimefrom",outArrMin);
        uriBuilder.appendQueryParameter("atimeto", outArrMax);
        uriBuilder.appendQueryParameter("returndtimefrom", retDepMin);
        uriBuilder.appendQueryParameter("returndtimeto", retDepMax);
        uriBuilder.appendQueryParameter("returnatimefrom",retArrMin);
        uriBuilder.appendQueryParameter("returnatimeto", retArrMax);
        uriBuilder.appendQueryParameter("partner", "picky");
        uriBuilder.appendQueryParameter("curr", "GBP");
        //uriBuilder.appendQueryParameter("selectedAirlines", "BA,IB,AY");
        //uriBuilder.appendQueryParameter("selectedAirlinesExclude", "False");
        uriBuilder.appendQueryParameter("limit", "100");
        uriBuilder.appendQueryParameter("sort", "price");
        uriBuilder.appendQueryParameter("asc", "1");

        String finalURI = uriBuilder.toString();
        Log.e(LOG_TAG, "Final url generated: " + finalURI);

        return new TripLoader(this, finalURI);
        }

    @Override
    public void onLoadFinished(Loader<List<Trip>> loader, List<Trip> trips) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_spinner);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No trips found!"
        mEmptyStateTextView.setText(R.string.no_trips);

        // If there is a valid list of {@link Trip}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (trips != null && !trips.isEmpty()) {
            //trips added to the adapter and displayed on the screen
            tAdapter.addAll(trips);
            // sorting all the results based on price
            // https://stackoverflow.com/questions/40143232/how-do-i-sort-the-content-of-my-custom-arrayadapter-by-a-variable-of-the-object
            tAdapter.sort(new Comparator<Trip>() {
                public int compare(Trip t1, Trip t2) {
                    // Need to use Integer at first for primitive type!
                    return Integer.valueOf(t1.getPrice()).compareTo(t2.getPrice());
                }
            });

        }
    }



    @Override
    public void onLoaderReset(Loader<List<Trip>> loader) {
        // Loader reset, so we can clear out our existing data.
        // clearing the adapter after each of loader iterations not to add to previous searches
        tAdapter.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreate started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_activity);

        /**
         * data from the MainActivity passed through the bundle
         */
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        depLoc = bundle.getString("dloc");
        holLoc = bundle.getString("hloc");
        depLocName = bundle.getString("dlocn");
        holLocName = bundle.getString("hlocn");
        fromDate = bundle.getString("ddate");
        toDate = bundle.getString("rdate");
        dirOnly = bundle.getBoolean("dir");
        oneWay = bundle.getBoolean("ow");
        langLoc = bundle.getString("lang");
        depString = bundle.getString("dstring");
        retString = bundle.getString("rstring");
        outDepMax = bundle.getString("oDMax");
        outDepMin = bundle.getString("oDMin");
        outArrMax = bundle.getString("oAMax");
        outArrMin = bundle.getString("oAMin");
        retDepMax = bundle.getString("rDMax");
        retDepMin = bundle.getString("rDMin");
        retArrMax = bundle.getString("rAMax");
        retArrMin = bundle.getString("rAMin");

        //Log.e(LOG_TAG, "testMin from tripactivity (1000): " + testMin);
        //Log.e(LOG_TAG, "testMax from tripactivity (2200): " + testMax);

        Log.e(LOG_TAG, "depString passed into trip activity" + depString);

        Scanner sD = new Scanner(depString);
        while(sD.hasNext()){
            depDates.add(sD.next());
        }
        sD.close();

        Scanner sR = new Scanner(retString);
        while(sR.hasNext()) {
            retDates.add(sR.next());
        }
        sR.close();
        Log.e(LOG_TAG, "depDates arrayList: " + depDates);
        Log.e(LOG_TAG, "retDates arrayList: " + retDates);



        cityToCity = (TextView) findViewById(R.id.city_to_city);
        dateToDate = (TextView) findViewById(R.id.date_to_date);
        String cToC = depLocName + " to " + holLocName;
        cityToCity.setText(cToC);
        String dToD = "between " + fromDate + " and " + toDate;
        dateToDate.setText(dToD);

        //todo: add on click listener on cityToCity and DateToDate to go back to search options and make filter to remember preferences

        Log.e(LOG_TAG, "bundle extras out");
        Log.e(LOG_TAG, "depLoc is " + depLoc);

        // Find a reference to the {@link ListView} in the layout
        ListView tripListView = (ListView) findViewById(R.id.list);


        // Create a new adapter that takes the list of trips as input
        tAdapter = new TripAdapter(this, new ArrayList<Trip>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        tripListView.setAdapter(tAdapter);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        tripListView.setEmptyView(mEmptyStateTextView);

        //************************************* dialog fragment **********************************

        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current trip that was clicked on
                Trip currentTrip = tAdapter.getItem(position);

                SummaryDialog summary = new SummaryDialog();

                Bundle bundle = new Bundle();

                //getting all the data from the adapter to be passed to the dialog
                bundle.putString("uri", currentTrip.getBookingURL());
                bundle.putString("depc", currentTrip.getDepCity());
                bundle.putString("holc", currentTrip.getArrCity());
                bundle.putString("country", currentTrip.getArrCountry());
                bundle.putInt("price", currentTrip.getPrice());

                List<Flight> flights = currentTrip.getFlights();
                Flight outbound = null;
                Flight returnn = null;


                if(flights.size()>=2) {
                    bundle.putBoolean("return", true);
                    outbound = flights.get(0);
                    bundle.putString("airFO", outbound.getDepAirportCode());
                    bundle.putString("airTO", outbound.getArrAirportCode());
                    bundle.putString("airlineO", outbound.getAirlineCode());

                    durationOut = secToDuration(currentTrip.getDepDuration());
                    bundle.putString("durO", durationOut);
                    Date outDeparture = new Date(outbound.getDepTimeInSeconds()*1000L - 3600000);
                    Date outArrival = new Date(outbound.getArrTimeInSeconds()*1000L - 3600000);
                    depTimeOut = formatTime(outDeparture);
                    arrTimeOut = formatTime(outArrival);
                    depDateOut = formatDate(outDeparture);
                    arrDateOut = formatDate(outArrival);
                    Log.e(LOG_TAG, "arrDateOut is: " + arrDateOut);

                    bundle.putString("timeDO", depTimeOut);
                    bundle.putString("timeAO", arrTimeOut);
                    bundle.putString("dateDO", depDateOut);
                    bundle.putString("dateAO", arrDateOut);

                    returnn = flights.get(1);
                    bundle.putString("airFR", returnn.getDepAirportCode());
                    bundle.putString("airTR", returnn.getArrAirportCode());
                    bundle.putString("airlineR", returnn.getAirlineCode());

                    durationRet = secToDuration(currentTrip.getRetDuration());
                    bundle.putString("durR", durationRet);
                    Date retDeparture = new Date(returnn.getDepTimeInSeconds()*1000L - 3600000);
                    Date retArrival = new Date(returnn.getArrTimeInSeconds()*1000L - 3600000);
                    depTimeRet = formatTime(retDeparture);
                    arrTimeRet = formatTime(retArrival);
                    depDateRet = formatDate(retDeparture);
                    arrDateRet = formatDate(retArrival);
                    Log.e(LOG_TAG, "arrDateRet is: " + arrDateRet);

                    bundle.putString("timeDR", depTimeRet);
                    bundle.putString("timeAR", arrTimeRet);
                    bundle.putString("dateDR", depDateRet);
                    bundle.putString("dateAR", arrDateRet);

                    summary.setArguments(bundle);
                    summary.show(getSupportFragmentManager(),"Summary");

                }else if (flights.size()==1) {
                    bundle.putBoolean("return", false);
                    outbound = flights.get(0);
                    bundle.putString("airFO", outbound.getDepAirportCode());
                    bundle.putString("airTO", outbound.getArrAirportCode());
                    bundle.putString("airlineO", outbound.getAirlineCode());

                    durationOut = secToDuration(currentTrip.getDepDuration());
                    bundle.putString("durO", durationOut);
                    Date outDeparture = new Date(outbound.getDepTimeInSeconds()*1000L - 3600000);
                    Date outArrival = new Date(outbound.getArrTimeInSeconds()*1000L - 3600000);
                    depTimeOut = formatTime(outDeparture);
                    arrTimeOut = formatTime(outArrival);
                    depDateOut = formatDate(outDeparture);
                    arrDateOut = formatDate(outArrival);

                    bundle.putString("timeDO", depTimeOut);
                    bundle.putString("timeAO", arrTimeOut);
                    bundle.putString("dateDO", depDateOut);
                    bundle.putString("dateAO", arrDateOut);

                    summary.setArguments(bundle);
                    summary.show(getSupportFragmentManager(),"Summary");
                }else {
                    outbound = null;
                }


                /*
                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookingUri = Uri.parse(currentTrip.getBookingURL());

                // Create a new intent to view the booking URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookingUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
                */
            }
        });

        //********************************** Networking *******************************************


        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).

            // initialises the actual loader with first 3 methods in this class (one calling triploader)
            // and does that for each date pair --> each having own loader to execute on the background
            for(int x = 0; x < depDates.size(); x++) {
                trip_loader_id = x;
                loaderManager.initLoader(trip_loader_id, null, this);
            }

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }

        //TODO: deal with http 400 response code


    }
}

