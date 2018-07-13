package com.example.android.flydays;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class TripLoader extends AsyncTaskLoader<List<Trip>> {

    private String Url;
    private static final String LOG_TAG = TripLoader.class.getName();

    /**
     * Trip loader that uses loadInBackground which is sourced from queryUtils.
     * It's activated from TripActivity in onCreateLoader
     */
    public TripLoader(Context context, String url){
        super(context);
        this.Url=url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Trip> loadInBackground() {
        // Don't perform the request if there are no URLs, or the first URL is null.
        if (Url == null) {
            return null;
        }
        //todo: addAll arrayLists to the result array for each url sent - how to then reset the trips list?
        List<Trip> result = FlightQueryUtils.fetchTripData(Url);
        Log.e(LOG_TAG, "Fetching trip data in TripLoader");
        return result;
    }
}
