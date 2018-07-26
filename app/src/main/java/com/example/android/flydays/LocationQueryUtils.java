package com.example.android.flydays;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class LocationQueryUtils {
    /**
     * Create a private constructor because no one should ever create a {@link LocationQueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name LocationQueryUtils (and an object instance of FlightQueryUtils is not needed).
     */
    private LocationQueryUtils() {
    }

    private static final String LOG_TAG = LocationQueryUtils.class.getName();




    /**
     * Query the kiwi flight info and return a list of {@link Trip} objects.
     */
    public static Location fetchLocationData(String requestUrl) {
        /* for testing whether the progressBar works the thread goes to sleep for 2000ms
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonLocations = null;
        try {
            jsonLocations = makeHttpRequest(url);
            Log.e(LOG_TAG, "LocationQueryUtils made Http request");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Trip}s
        Location locsAvailable = extractLocsFromJson(jsonLocations);
        Log.e(LOG_TAG, "LocationQueryUtils makeHTTP request extracted locations");

        // Return the list of {@link Location}s
        Log.e(LOG_TAG, "LocationQueryUtils Http request returns locsAvailable");
        return locsAvailable;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonLocations = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonLocations;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonLocations = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the location JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        Log.e(LOG_TAG, "JSON is : " + jsonLocations);
        return jsonLocations;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Trip} objects that has been built up from
     * parsing the given JSON response.
     */
    private static Location extractLocsFromJson(String locJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(locJSON)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding trips to
        //List<Location> locations = new ArrayList<>();
        ArrayList<String> locsAvailable = new ArrayList<>();;
        Map <String, String> locsMap = new HashMap();
        Location locs = null;

        //List<String> locsList = new ArrayList<String>();


        // Try to parse the JSON string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            //Convert tripJson String into a JSONObject
            JSONObject baseJsonResponse = new JSONObject(locJSON);

            JSONArray locArray = baseJsonResponse.getJSONArray("locations");
            for (int x = 0; x<locArray.length(); x++){
                JSONObject currentAirport = locArray.getJSONObject(x);

                String airportC = currentAirport.getString("id");
                String airportN = currentAirport.getString("name");
                String timezn = currentAirport.getString("timezone");

                JSONObject city = currentAirport.getJSONObject("city");
                String cityC = city.getString("code"); //tried ID but that one didn't work for some cities
                String cityN = city.getString("name");
                JSONObject country = city.getJSONObject("country");
                String countryC = country.getString("id");
                String countryN = country.getString("name");


                //locations.add(location);


                locsMap.put(airportN, airportC);
                locsMap.put(cityN, cityC);
                locsMap.put(countryN, countryC);

                //todo: add the locations here and use this set for the adapter ---> how to map city to code etc?
                //map object
                // distinct list --> set https://stackoverflow.com/questions/13429119/get-unique-values-from-arraylist-in-java
                // making my own list
                if(!locsAvailable.contains(airportN))
                    locsAvailable.add(airportN);
                if(!locsAvailable.contains(cityN))
                    locsAvailable.add(cityN);
                if(!locsAvailable.contains(countryN))
                    locsAvailable.add(countryN);
            }
            locsMap.put("anywhere", "anywhere");
            //locsAvailable.add("anywhere");

            locs = new Location(locsAvailable,locsMap);
            //Log.e(LOG_TAG, "locsAvailable array: " + locsAvailable);
            //Log.e(LOG_TAG, "locsMap: " + locsMap);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("LocationQueryUtils", "Problem parsing the location JSON results", e);
        }

        // Return the list of trips
        return locs;
    }





}

