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
import java.util.List;


public final class FlightQueryUtils {
    /**
     * Create a private constructor because no one should ever create a {@link FlightQueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name FlightQueryUtils (and an object instance of FlightQueryUtils is not needed).
     */
    private FlightQueryUtils() {
    }

    private static final String LOG_TAG = FlightQueryUtils.class.getName();


    /**
     * Query the kiwi flight info and return a list of {@link Trip} objects.
     *
     * all methods from this class are being used in this method, initialised from TripLoader
     */
    public static List<Trip> fetchTripData(String requestUrl) {
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
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
            Log.e(LOG_TAG, "FlightQueryUtils made Http request");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Trip}s
        List<Trip> trips = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Trip}s
        return trips;
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
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(15000 /* milliseconds */);
            urlConnection.setConnectTimeout(20000 /* milliseconds */);
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the flight JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
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
    private static List<Trip> extractFeatureFromJson(String tripJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(tripJSON)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding trips to
        List<Trip> trips = new ArrayList<>();


        // Try to parse the JSON string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            //Convert tripJson String into a JSONObject
            JSONObject baseJsonResponse = new JSONObject(tripJSON);

            JSONObject searchParam = baseJsonResponse.getJSONObject("search_params");
            String destType = searchParam.getString("to_type");
            Log.e(LOG_TAG, "destTYpe in FlightQueryUtils " + destType);

            //obsolete as it's always in currency specified in a call by default -- GBP
            String currency = baseJsonResponse.getString("currency");

            //Extract “data” JSONArray
            JSONArray tripArray = baseJsonResponse.getJSONArray("data");
            //Loop through each flight in the array
            for (int i = 0; i < tripArray.length();i++){

                JSONObject currentTrip = tripArray.getJSONObject(i);


                int price = currentTrip.getInt("price");
                String bookingUrl = currentTrip.getString("deep_link");
                String depCity = currentTrip.getString("cityFrom");
                String arrCity = currentTrip.getString("cityTo");
                JSONObject arrCountryObject = currentTrip.getJSONObject("countryTo");
                String arrCountry = arrCountryObject.getString("name");

                JSONObject duration = currentTrip.getJSONObject("duration");
                long depDur = duration.getLong("departure");
                long retDur = duration.getLong("return");

                JSONArray flightArray = currentTrip.getJSONArray("route");
                List<Flight> flights = new ArrayList<>();
                for(int x = 0; x < flightArray.length(); x++){
                    JSONObject currentFlight = flightArray.getJSONObject(x);

                    String depAirportCode = currentFlight.getString("flyFrom");
                    String arrAirportCode = currentFlight.getString("flyTo");
                    long depTime = currentFlight.getLong("dTime"); //local time
                    long arrTime = currentFlight.getLong("aTime"); //local time
                    String airlineCode = currentFlight.getString("airline");
                    String flightNo = currentFlight.getString("flight_no");


                    Flight flight = new Flight(depAirportCode, arrAirportCode, depTime, arrTime,
                                airlineCode, flightNo);
                    flights.add(flight);
                }

                Trip trip = new Trip(flights, price, currency, destType, bookingUrl, depCity, arrCity, arrCountry,
                        depDur, retDur);
                trips.add(trip);

            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("FlightQueryUtils", "Problem parsing the trip JSON results", e);
        }

        // Return the list of trips
        return trips;
    }



}
