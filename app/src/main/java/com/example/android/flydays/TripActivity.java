package com.example.android.flydays;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TripActivity extends AppCompatActivity implements View.OnClickListener {

//todo: PROBLEM with API calls for 'anywhere' as only the cheapest flights are listed for each destination which means that when I filter them by time there are not many left
//todo: problem with locations in Belgium, all marked as City of London, mistake in kiwi documentation.
    /** base URL for trip data from the Kiwi dataset */
    private static final String FLIGHT_REQUEST_URL =
            "https://api.skypicker.com/flights";

    private static final String AIRLINES_URL =
            "https://api.skypicker.com/airlines";

    private static final String CITY_IMAGE_URL =
            "https://api.teleport.org/api/urban_areas/?embed=ua:item/ua:images";

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

    private Map<String,String> airlines;
    private Map<String,String> cityPicURL;
    private HashMap<String,String> airports;
    private Bitmap cityPic;
    private String imageUrl;

    AirlinesCityurlLoader loader;


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
    private String airlineOutN;
    private String airlineBackN;
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

    private View infoPanel;

    private final int AIRLINES_LOADER_ID = 100;
    private final int CITYURL_LOADER_ID = 200;


    //todo: change in report -- the way multiple Loaders are structured
    // to create multiple loaders: Adil Hussain https://stackoverflow.com/questions/15643907/multiple-loaders-in-same-activity
    private LoaderCallbacks<List<Trip>> loadTrips = new LoaderCallbacks<List<Trip>>() {
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
            uriBuilder.appendQueryParameter("flyFrom", depLoc);
            uriBuilder.appendQueryParameter("to", holLoc);
            uriBuilder.appendQueryParameter("dateFrom", depDates.get(trip_loader_id)); //use triploaderID here?
            uriBuilder.appendQueryParameter("dateTo", depDates.get(trip_loader_id));
            //can get array out of bounds exception if there is nothing to get from retDates
            //todo: change in report -- return dates only added to uri if there is return
            if(!oneWay){
                uriBuilder.appendQueryParameter("returnFrom", retDates.get(trip_loader_id));
                uriBuilder.appendQueryParameter("returnTo",retDates.get(trip_loader_id));
            }
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

            return new TripLoader(getApplicationContext(), finalURI);
        }

        @Override
        public void onLoadFinished(Loader<List<Trip>> loader, List<Trip> trips) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);


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
            }else{
                // Set empty state text to display "No trips found!"
                // This won't be visible unless the trips list is empty - if response code is not 200 or there are no actual trips found
                //todo: change in report -- if there are no trips in trips list it shows no trips found
                mEmptyStateTextView.setText(R.string.no_trips);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Trip>> loader) {
            // Loader reset, so we can clear out our existing data.
            // clearing the adapter after each of loader iterations not to add to previous searches
            tAdapter.clear();
        }
    };


    private LoaderCallbacks<Map<String,String>> loadAirlinesOrCityURL = new LoaderCallbacks<Map<String, String>>() {
        @Override
        public Loader<Map<String,String>> onCreateLoader(int i, Bundle bundle) {
            switch (i){
                case AIRLINES_LOADER_ID:
                    loader = new AirlinesCityurlLoader(getApplicationContext(), AIRLINES_URL);
                    break;
                case CITYURL_LOADER_ID:
                    loader = new AirlinesCityurlLoader(getApplicationContext(), CITY_IMAGE_URL);
                    break;
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Map<String,String>> lLoader, Map<String, String> map) {
            switch (lLoader.getId()){
                case AIRLINES_LOADER_ID:
                    airlines = map;
                    if (airlines != null) {
                        //https://developer.android.com/training/data-storage/files#java
                        try{
                            File file = File.createTempFile("airlines", null, getApplicationContext().getCacheDir());
                            FileOutputStream outputStream = openFileOutput("airlines", Context.MODE_PRIVATE);
                            //wrapping it into object output stream https://mkyong.com/java/how-to-write-an-object-to-file-in-java/
                            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                            oos.writeObject(airlines);
                            outputStream.close();
                            oos.close();
                        }catch (IOException e){
                            Log.e(LOG_TAG, "problems with a file in cacheDir " + e);
                        }
                    }
                    break;
                case CITYURL_LOADER_ID:
                    cityPicURL=map;
                    if (cityPicURL != null) {
                        //https://developer.android.com/training/data-storage/files#java
                        try{
                            File file = File.createTempFile("cityPicUrl", null, getApplicationContext().getCacheDir());
                            FileOutputStream outputStream = openFileOutput("cityPicUrl", Context.MODE_PRIVATE);
                            //wrapping it into object output stream https://mkyong.com/java/how-to-write-an-object-to-file-in-java/
                            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                            oos.writeObject(cityPicURL);
                            outputStream.close();
                            oos.close();
                        }catch (IOException e){
                            Log.e(LOG_TAG, "problems with a file in cacheDir " + e);
                        }
                    }
                    break;
            }

        }

        @Override
        public void onLoaderReset(Loader<Map<String,String>> loader) {    }
    };



/*
    LoaderCallbacks<Bitmap> images = new LoaderCallbacks<Bitmap>() {
        @Override
        public Loader<Bitmap> onCreateLoader(int i, Bundle bundle) {

            ImageLoader bitLoader = new ImageLoader(getApplicationContext(),imageUrl);
            return bitLoader;
        }

        @Override
        public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
            cityPic = bitmap;
        }

        @Override
        public void onLoaderReset(Loader<Bitmap> loader) {        }
    };
    */

    //to go back and edit search parameters
    @Override
    public void onClick(View view){
        finish();
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


//**************************************** on create starts ****************************************
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
        airports = (HashMap<String,String>) bundle.getSerializable("map");

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
        //todo: change in report -- on click listener on cityToCity and DateToDate to go back to search params
        //sets onClickListener on top information panel so when it's clicked it would bring the user back to MainActivity
        infoPanel = findViewById(R.id.info_panel);
        infoPanel.setOnClickListener(this);




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


        //todo: change in report -- another mechanism to ensure dates are selected
        //if there are no departure dates (e.g. because range is smaller than trip lenght) it would show "No trips found!"
        if(depDates.isEmpty()){
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            mEmptyStateTextView.setText(R.string.no_trips);
        }

        //************************************* dialog fragment **********************************

        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current trip that was clicked on
                Trip currentTrip = tAdapter.getItem(position);
                try{
                    imageUrl = cityPicURL.get(currentTrip.getArrCity());
                }catch(Exception e){
                    Log.e(LOG_TAG, "city not found in cityPicURL");
                }

                //getLoaderManager().initLoader(101, null, images);

                SummaryDialog summary = new SummaryDialog();

                Bundle bundle = new Bundle();

                //getting all the data from the adapter to be passed to the dialog
                bundle.putString("uri", currentTrip.getBookingURL());
                bundle.putString("depc", currentTrip.getDepCity());
                bundle.putString("holc", currentTrip.getArrCity());
                bundle.putString("country", currentTrip.getArrCountry());
                bundle.putInt("price", currentTrip.getPrice());
                bundle.putString("cityurl", imageUrl);

                List<Flight> flights = currentTrip.getFlights();
                Flight outbound = null;
                Flight returnn = null;


                if(flights.size()>=2) {
                    bundle.putBoolean("return", true);
                    outbound = flights.get(0);
                    bundle.putString("airFO", airports.get(outbound.getDepAirportCode()));
                    bundle.putString("airTO", airports.get(outbound.getArrAirportCode()));
                    bundle.putString("airlineCodeO", outbound.getAirlineCode());
                    //in case that search results are clicked before loadAirlinesOrCityURL finishes with airlines map object
                    try{
                        bundle.putString("airlineO", airlines.get(outbound.getAirlineCode()));
                    }catch (Exception e){
                        Log.e(LOG_TAG, "problem with reading airlines map ", e);
                        bundle.putString("airlineO", outbound.getAirlineCode());
                    }

                    durationOut = secToDuration(currentTrip.getDepDuration());
                    bundle.putString("durO", durationOut);
                    Date outDeparture = new Date(outbound.getDepTimeInSeconds()*1000L - 3600000L);
                    Date outArrival = new Date(outbound.getArrTimeInSeconds()*1000L - 3600000L);
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
                    bundle.putString("airFR", airports.get(returnn.getDepAirportCode()));
                    bundle.putString("airTR", airports.get(returnn.getArrAirportCode()));
                    bundle.putString("airlineCodeR", returnn.getAirlineCode());
                    try {
                        bundle.putString("airlineR", airlines.get(returnn.getAirlineCode()));
                    }catch (Exception e){
                        bundle.putString("airlineR", returnn.getAirlineCode());
                    }
                    durationRet = secToDuration(currentTrip.getRetDuration());
                    bundle.putString("durR", durationRet);
                    Date retDeparture = new Date(returnn.getDepTimeInSeconds()*1000L - 3600000L);
                    Date retArrival = new Date(returnn.getArrTimeInSeconds()*1000L - 3600000L);
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
                    bundle.putString("airFO", airports.get(outbound.getDepAirportCode()));
                    bundle.putString("airTO", airports.get(outbound.getArrAirportCode()));
                    bundle.putString("airlineCodeO", outbound.getAirlineCode());
                    try{
                        bundle.putString("airlineO", airlines.get(outbound.getAirlineCode()));
                    }catch (Exception e){
                        Log.e(LOG_TAG, "problem with reading airlines map ", e);
                        bundle.putString("airlineO", outbound.getAirlineCode());
                    }

                    durationOut = secToDuration(currentTrip.getDepDuration());
                    bundle.putString("durO", durationOut);
                    Date outDeparture = new Date(outbound.getDepTimeInSeconds()*1000L - 3600000L);
                    Date outArrival = new Date(outbound.getArrTimeInSeconds()*1000L - 3600000L);
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
                loaderManager.initLoader(trip_loader_id, null, loadTrips);
            }

            // initialises loader for airlines names
            try {
                //https://developer.android.com/training/data-storage/files#java
                FileInputStream fileIStream = openFileInput("airlines");
                //https://mkyong.com/java/how-to-read-an-object-from-file-in-java/
                ObjectInputStream ois = new ObjectInputStream(fileIStream);
                airlines = (Map<String, String>) ois.readObject();
                Log.e(LOG_TAG, "file was found for airlines map" + airlines);

            }catch (Exception e){
                loaderManager.initLoader(AIRLINES_LOADER_ID,null, loadAirlinesOrCityURL);
                Log.e(LOG_TAG, "loadAirlinesOrCityURL was triggered for airlines map");
            }

            // initialises loader for city picture URLs
            try{
                FileInputStream fileInStream = openFileInput("cityPicUrl");
                //https://mkyong.com/java/how-to-read-an-object-from-file-in-java/
                ObjectInputStream oIs = new ObjectInputStream(fileInStream);
                cityPicURL = (Map<String, String>) oIs.readObject();
                Log.e(LOG_TAG, "file was found for citypicurl map");
            }catch (Exception e){
                loaderManager.initLoader(CITYURL_LOADER_ID,null, loadAirlinesOrCityURL);
                Log.e(LOG_TAG, "loadAirlinesOrCityURL was triggered for citypicurl map");
            }


        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }
    }
}

