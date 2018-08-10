package com.example.android.flydays;


import android.content.AsyncTaskLoader;
import android.content.Context;
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
import java.util.HashMap;
import java.util.Map;

//todo: change in report -- add airlinesCityur loader to the report and everything associated with it in Trip activity

/**
 * Loader used for getting various information. This comes in two ways.
 * 1 - with Loader ID 100, returns a map <airline code , full name of airline>
 * 2 - with Loader ID 200, returns a map <city , picture URL>
 */
public class AirlinesCityurlLoader extends AsyncTaskLoader<Map<String,String>> {

    private String Uri;
    private static final String LOG_TAG = AirlinesCityurlLoader.class.getName();

    public AirlinesCityurlLoader(Context context, String url) {
        super(context);
        this.Uri = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Map<String, String> loadInBackground() {
        URL url;
        try {
            url = new URL(Uri);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }


        String json = "";
        //establish connection
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
                json = readFromStream(inputStream);
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
                try {
                    inputStream.close();
                } catch (IOException io) {
                    Log.e(LOG_TAG, "Problem with input stream", io);
                }
            }
        }

        if (TextUtils.isEmpty(json)) {
            return null;
        }

        Map<String, String> airlinesOrCityurlMap = new HashMap();


        switch (getId()){
            case 100:
                Log.e(LOG_TAG, "we are in case 100 indeed -- airlines");
                try {
                    JSONArray airlineArray = new JSONArray(json);
                    for (int x = 0; x < airlineArray.length(); x++) {
                        JSONObject currentAirline = airlineArray.getJSONObject(x);

                        String airlineID = currentAirline.getString("id");
                        String airlineN = currentAirline.getString("name");


                        airlinesOrCityurlMap.put(airlineID, airlineN);
                    }
                } catch (JSONException e) {
                    // If an error is thrown when executing any of the above statements in the "try" block,
                    // catch the exception here, so the app doesn't crash. Print a log message
                    // with the message from the exception.
                    Log.e("AirlinesCityurlLoader", "Problem parsing the airlines JSON results", e);
                }
                break;
            case 200:
                Log.e(LOG_TAG, "we are in case 200 indeed -- pic URL");
                try {
                    //documentation for getting city url images: https://developers.teleport.org/api/getting_started/#photos_ua
                    JSONObject baseObject = new JSONObject(json);
                    JSONObject embeddedObject = baseObject.getJSONObject("_embedded");
                    JSONArray itemArray = embeddedObject.getJSONArray("ua:item");
                    for (int x = 0; x < itemArray.length(); x++) {
                        JSONObject currentCity = itemArray.getJSONObject(x);

                        JSONObject embedded = currentCity.getJSONObject("_embedded");
                        JSONObject images = embedded.getJSONObject("ua:images");
                        JSONArray photoArray = images.getJSONArray("photos");
                        JSONObject photoObject = photoArray.getJSONObject(0);
                        JSONObject photo = photoObject.getJSONObject("image");
                        String imageURL = photo.getString("web");


                        JSONObject linksObject = currentCity.getJSONObject("_links");
                        JSONObject cityObject = linksObject.getJSONObject("ua:identifying-city");
                        String cityName = cityObject.getString("name");

                        //other way in doing this but then it can't get pics with special characters like Gdańsk.
                        //String cityName = currentCity.getString("name");


                        airlinesOrCityurlMap.put(cityName, imageURL);
                    }
                } catch (JSONException e) {
                    // If an error is thrown when executing any of the above statements in the "try" block,
                    // catch the exception here, so the app doesn't crash. Print a log message
                    // with the message from the exception.
                    Log.e("AirlinesCityurlLoader", "Problem parsing the cityURL JSON results", e);
                }
                break;
            default:
        }

        return airlinesOrCityurlMap;
    }

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
}
