package com.example.android.flydays;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocationLoader extends AsyncTaskLoader<Location> {

    private String Url;
    private static final String LOG_TAG = LocationLoader.class.getName();

    public LocationLoader(Context context, String url){
        super(context);
        this.Url=url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Location loadInBackground() {
        // Don't perform the request if there are no URLs, or the first URL is null.
        if (Url == null) {
            return null;
        }
        Location result = LocationQueryUtils.fetchLocationData(Url);
        Log.e(LOG_TAG, "Fetching location data in LocationLoader");
        return result;
    }
}