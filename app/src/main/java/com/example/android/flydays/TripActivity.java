package com.example.android.flydays;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class TripActivity extends AppCompatActivity implements LoaderCallbacks<List<Trip>> {

    //todo: make as many calls as required
    //todo: order the results of the calls by price

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
    private String depDate;
    private String retDate;
    private boolean dirOnly;
    private boolean oneWay;
    private String langLoc;
    private String depString;
    private String retString;
    private ArrayList<String> depDates = new ArrayList<>();
    private ArrayList<String> retDates = new ArrayList<>();

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

        //todo: put his in a method with params for array lists of dates
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
        uriBuilder.appendQueryParameter("partner", "picky");
        uriBuilder.appendQueryParameter("limit", "30");
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
        /**
         * data from the MainActivity passed through the bundle
         */
        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        depLoc = bundle.getString("dloc");
        holLoc = bundle.getString("hloc");
        depDate = bundle.getString("ddate");
        retDate = bundle.getString("rdate");
        dirOnly = bundle.getBoolean("dir");
        oneWay = bundle.getBoolean("ow");
        langLoc = bundle.getString("lang");
        depString = bundle.getString("dstring");
        retString = bundle.getString("rstring");

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


        setContentView(R.layout.trip_activity);

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

        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Trip currentTrip = tAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookingUri = Uri.parse(currentTrip.getBookingURL());

                // Create a new intent to view the booking URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookingUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });


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

        // Start the AsyncTask to fetch the earthquake data
        /*
        EarthquakeAsyncTask task = new EarthquakeAsyncTask();
        task.execute(USGS_REQUEST_URL);
        */
    }
}
