package com.example.android.flydays;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TripActivity extends AppCompatActivity implements LoaderCallbacks<List<Trip>> {

    /** base URL for trip data from the Kiwi dataset */
    private static final String FLIGHT_REQUEST_URL =
            "https://api.skypicker.com/flights";

    /** Adapter for the list of trips */
    private TripAdapter tAdapter;

    /**
     * Constant value for the trip loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int TRIP_LOADER_ID = 1;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    private static final String LOG_TAG = TripActivity.class.getName();

    private String depLoc;
    private String holLoc;
    private String depDate;
    private String retDate;
    private boolean dirOnly;

        /**
     * Creates the url based on established preferences and uses it for API call to update
     * the search results
     */
    @Override
    public Loader<List<Trip>> onCreateLoader(int i, Bundle bundle) {

        // Create a new loader for the given URL
     /*   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        Uri baseUri = Uri.parse(FLIGHT_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
     */

        Uri baseUri = Uri.parse(FLIGHT_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        String onewayOrReturn = retDate==null ? "oneway" : "round";
        String directOrNot = dirOnly ? "1" : "0";
        /*
        basic example:
        https://api.skypicker.com/flights?flyFrom=LON&to=anywhere&dateFrom=13/08/2018&dateTo=13/08/2018&
        returnFrom=15/08/2018&returnTo=15/08/2018&maxFlyDuration=3&typeFlight=round&directFlights=1&
        locale=en&partner=picky&price_to=100&limit=30&sort=price&asc=1
         */

        uriBuilder.appendQueryParameter("flyFrom", depLoc);
        uriBuilder.appendQueryParameter("to", holLoc);
        uriBuilder.appendQueryParameter("dateFrom", depDate);
        uriBuilder.appendQueryParameter("dateTo", depDate);
        uriBuilder.appendQueryParameter("returnFrom", retDate);
        uriBuilder.appendQueryParameter("returnTo",retDate);
        uriBuilder.appendQueryParameter("typeFlight", onewayOrReturn);
        uriBuilder.appendQueryParameter("directFlights", directOrNot);
        uriBuilder.appendQueryParameter("locale", "en");
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

        // Clear the adapter of previous earthquake data
        tAdapter.clear();

        // If there is a valid list of {@link Trip}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (trips != null && !trips.isEmpty()) {
            tAdapter.addAll(trips);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Trip>> loader) {
        // Loader reset, so we can clear out our existing data.
        tAdapter.clear();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
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
            loaderManager.initLoader(TRIP_LOADER_ID, null, this);
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
