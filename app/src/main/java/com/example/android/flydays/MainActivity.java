package com.example.android.flydays;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.TextView;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
//todo: edit trip activity to show one way flights correctly
//todo: create an introduction page (with logo) for location api to be called
//todo: add extra filtering options
//todo: create a custom adapter? to sort locations better
    //https://stackoverflow.com/questions/11574752/autocompletetextview-doesnt-suggest-what-i-want


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderCallbacks<Location>{

    AutoCompleteTextView locFromView;
    AutoCompleteTextView locToView;
    EditText minDateText;
    EditText maxDateText;
    CheckBox dirOnlyBox;
    CheckBox oneWayOnly;
    EditText daysInText;
    Button searchButton;

    String langLocale;

    private int daysIn=0;

    private long min;
    private long max;
    private ArrayList<Long> datesArray;
    private String depDates = "";
    private String retDates = "";

    private int dYear, dMonth, dDay;
    //day of the week 1-7, where 1 is Sunday
    private int dayWeek;
    private int dayWanted;

    private ArrayAdapter<String> adapterFrom;
    private ArrayAdapter<String> adapterTo;
    private ArrayList<String> locsList;
    private Map <String, String> locsMap;

    //week buttons
    private Button[] btn = new Button[7];
    private Button btn_unfocus;
    private int[] btn_id = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6};


    final Calendar c = Calendar.getInstance();
    //long retInMillis;

    private DatePickerDialog datePickerDialog2 = null;
    private DatePickerDialog datePickerDialog = null;

    private static final String LOCATIONS_REQUEST_URL =
            "https://api.skypicker.com/locations";

    private static final int LOCATION_LOADER_ID = 1;

    private static final String LOG_TAG = MainActivity.class.getName();



//***************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //muscardinus at https://stackoverflow.com/questions/4212320/get-the-current-language-in-device
        langLocale = Locale.getDefault().getLanguage();
        //Log.e(LOG_TAG, "landLocale is: " + langLocale);


/*      todo: make location work
        ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                1 );
        //LocationResult locationResult = new LocationResult(){
        @Override
        public void gotLocation(Location location){
            currLocation = (String) location;
        }


        //};
        UserLocation myLocation = new UserLocation();
        myLocation.getLocation(this, locationResult);
*/


        minDateText = findViewById(R.id.departure_date);
        //todo: maxDateText might not be within the range, change the wording or not let corresponding depString pass?
        maxDateText = findViewById(R.id.return_date);

        minDateText.setOnClickListener(this);
        maxDateText.setOnClickListener(this);

        dirOnlyBox = findViewById(R.id.direct_checkbox);

        daysInText = (EditText) findViewById(R.id.days_in);
        oneWayOnly = findViewById(R.id.one_way);
        oneWayOnly.setOnClickListener(this);


        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);

        //needed for the datePickerDialog to have current date
        dYear = c.get(Calendar.YEAR);
        dMonth = c.get(Calendar.MONTH);
        dDay = c.get(Calendar.DAY_OF_MONTH);
        //dayWeek = c.get(Calendar.DAY_OF_WEEK);
        //Log.e(LOG_TAG, "calendar c after creation" + c);


        //buttons for days, xml and code from https://stackoverflow.com/questions/32534076/what-is-the-best-way-to-do-a-button-group-that-can-be-selected-and-activate-inde
        for(int i = 0; i < btn.length; i++){
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].setBackgroundColor(Color.rgb(255, 255, 255));
            btn[i].setOnClickListener(this);
        }
        btn_unfocus = btn[0];


        //todo: put an api call into a cache -- explore different strategies on what is most suitable

        // what is needed for autoCompleteTextView: https://developer.android.com/training/keyboard-input/style

        // Get a reference to the AutoCompleteTextView in the layout
        locFromView = findViewById(R.id.location_from);
        locToView = findViewById(R.id.location_to);

        // Create the adapter and set it to the AutoCompleteTextView

        adapterFrom =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapterTo =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //todo: do I need two or just one is sufficient?
        locToView.setThreshold(1);
        locFromView.setThreshold(1);
        locFromView.setAdapter(adapterFrom);
        locToView.setAdapter(adapterTo);

//***********************************Networking****************************************************

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
            loaderManager.initLoader(LOCATION_LOADER_ID, null, this);
        } else {
            // todo: enable hard typing?
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

        }




        //todo: go directly to the next field when finished with one
        //similar example: https://stackoverflow.com/questions/23123833/edittext-automatically-go-to-a-new-line


        locFromView.getText().clear();
        locToView.getText().clear();
        minDateText.getText().clear();
        maxDateText.getText().clear();
        dirOnlyBox.toggle();


    }


//**************************************************************************************************
    @Override
    public void onClick(View view) {


        //using switch to get the right onClick implementation for buttons
        switch (view.getId()) {

            case R.id.search_button:
                //activating methods to retrieve all possible dates that will be passed to tripactivity


                if (!oneWayOnly.isChecked()) {
                    daysIn = Integer.parseInt(daysInText.getText().toString());
                    getReturnDates(min, max, dayWeek, dayWanted, daysIn);

                }else{
                    getOnewayDates(min, max, dayWeek, dayWanted);
                }

                Log.e(LOG_TAG, "depDates string " + depDates);
                Log.e(LOG_TAG, "retDates string " + retDates);


                Intent search = new Intent(MainActivity.this, TripActivity.class);


                //on how to pass data:
                //https://stackoverflow.com/questions/3510649/how-to-pass-a-value-from-one-activity-to-another-in-android
                Bundle bundle = new Bundle();
                //Add your data to bundle
                bundle.putString("dloc", (locsMap.get((locFromView.getText()).toString())));
                bundle.putString("hloc", (locsMap.get((locToView.getText()).toString())));
                bundle.putString("ddate", (minDateText.getText()).toString());
                //todo: make a toast message if rdate is before ddate
                bundle.putString("rdate", (maxDateText.getText()).toString());
                //bundle.putString("days", daysInText.getText().toString());
                bundle.putBoolean("dir", dirOnlyBox.isChecked());
                bundle.putBoolean("ow", oneWayOnly.isChecked());
                bundle.putString("lang", langLocale);
                bundle.putString("dstring", depDates);
                bundle.putString("rstring", retDates);


                //Add the bundle to the intent
                search.putExtras(bundle);

                startActivity(search);
                break;

            case R.id.departure_date:
                //https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
                //code from Mohsin Bhat to add calendar dialog


                //activate the dialogue
                datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            //date is picked
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                //c becomes minimum date for selection in the return date
                                c.set(year, monthOfYear, dayOfMonth);
                                minDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                dayWeek = c.get(Calendar.DAY_OF_WEEK);
                                min = c.getTimeInMillis();
                                Log.e(LOG_TAG, "The day of the week is " + dayWeek);
                            }
                        }, dYear, dMonth, dDay);
                //todo: if going back from results this line causes illegal argument exception:
                //fromDate: Fri Jul 13 12:27:44 GMT+01:00 2018 does not precede toDate: Fri Jul 13 12:27:44 GMT+01:00 2018
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

                minDateText.setFocusable(false);
                datePickerDialog.setTitle("");
                datePickerDialog.show();
                break;
            case R.id.return_date:
                //activate the dialogue
                datePickerDialog2 = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                //month +1 as months are 0-11
                                maxDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                c.set(year, monthOfYear, dayOfMonth);
                                //Date maxDate = new Date (year, monthOfYear, dayOfMonth);
                                //max=maxDate.getTime();
                                max = c.getTimeInMillis();
                                c.setTimeInMillis(System.currentTimeMillis());

                            }
                        }, dYear, dMonth, dDay);

                datePickerDialog2.getDatePicker().setMinDate(c.getTimeInMillis());

                maxDateText.setFocusable(false);
                datePickerDialog2.setTitle("");
                datePickerDialog2.show();
                break;
            case R.id.one_way :
                if(oneWayOnly.isChecked())
                    daysInText.setText("oneway", TextView.BufferType.EDITABLE);
                break;
            case R.id.btn0 :
                setFocus(btn_unfocus, btn[0]);
                dayWanted=2;
                break;

            case R.id.btn1 :
                setFocus(btn_unfocus, btn[1]);
                dayWanted=3;
                break;

            case R.id.btn2 :
                setFocus(btn_unfocus, btn[2]);
                dayWanted=4;
                break;

            case R.id.btn3 :
                setFocus(btn_unfocus, btn[3]);
                dayWanted=5;
                break;

            case R.id.btn4 :
                setFocus(btn_unfocus, btn[4]);
                dayWanted=6;
                break;

            case R.id.btn5 :
                setFocus(btn_unfocus, btn[5]);
                dayWanted=7;
                break;

            case R.id.btn6 :
                setFocus(btn_unfocus, btn[6]);
                dayWanted=1;
                break;
            default:
                Log.e(LOG_TAG, "OnClick went to default");
                break;
        }
    }
//************************************METHODS********************************************************
    /**
     * Makes functionality for weekday buttons to be selected and deselected with following colours
     * @param btn_unfocus
     * @param btn_focus
     */
    private void setFocus(Button btn_unfocus, Button btn_focus){
        btn_unfocus.setTextColor(Color.rgb(49, 50, 51));
        btn_unfocus.setBackgroundColor(Color.rgb(255, 255, 255));
        btn_focus.setTextColor(Color.rgb(255, 255, 255));
        btn_focus.setBackgroundColor(Color.rgb(3, 106, 150));  //blue
        this.btn_unfocus = btn_focus;
    }


    //1 day is 86,400,000 milliseconds

    private String makeDateString(long dateInMilli) {
        Date dateObject = new Date (dateInMilli);
        Log.e(LOG_TAG, "dateobject: " + dateObject);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Finds and array of possible departure dates in milliseconds
     */
    //todo: deal with situation such as dayweek 7, dayWantd 6, daysIn int 2 - flying friday not sunday
    private void getReturnDates(long min, long max, int dayWeek, int dayWanted, int daysIn){
        //need to delete the string otherwise it would add to it after going back from results in the app
        depDates="";
        retDates="";
        int difference;
        if(dayWeek>dayWanted){
            difference=(7-dayWeek+dayWanted) * 86400000;
        }else if(dayWeek<dayWanted){
            difference = -(dayWeek-dayWanted) * 86400000;
        }else{
            difference=0;
        }

        long newDate = min + difference; //first day of possible departure
        Log.e(LOG_TAG, "dayweek " + dayWeek);
        Log.e(LOG_TAG, "dayWantd " + dayWanted);
        Log.e(LOG_TAG, "daysIn int " + daysIn);
        Log.e(LOG_TAG, "min " + min);
        Log.e(LOG_TAG, "max " + max);
        Log.e(LOG_TAG, "difference " + difference);
        Log.e(LOG_TAG, "newDate, first one " + newDate);
        while (newDate<max){
            String stringDateO = makeDateString(newDate);
            Log.e(LOG_TAG, "first date" + stringDateO);
            String stringDateI = makeDateString(newDate+(daysIn*86400000)); //outgoing plus days * milli
            Log.e(LOG_TAG, "first return date" + stringDateI);
            depDates += stringDateO + " ";
            retDates += stringDateI + " ";
            newDate += 7 * 86400000; //new date becomes another day of the week following week

        }


    }

    private void getOnewayDates(long min, long max, int dayWeek, int dayWanted){
        depDates = "";
        int difference = (dayWeek-dayWanted) * 86400000;
        if(difference<0)
            difference=difference*(-1);
        long newDate = min + difference; //first day of possible departure
        while (newDate<max){
            String stringDateO = makeDateString(newDate);
            depDates += stringDateO + " ";
            newDate += 7 * 86400000; //new date becomes another day of the week following week
        }
    }

    /*
    private ArrayList<Long> getDatesArray(long min, long max, int dayWeek, int dayWanted){
        datesArray = new ArrayList<>();
        int difference = (dayWeek-dayWanted) * 86400;
        if(difference<0)
            difference=difference*(-1);
        long newDate = min + difference;
        while(newDate<=max){
            datesArray.add(newDate);
            newDate= newDate + (7 * 86400);
        }
        return datesArray;
    }
     */

    //****************************************locations threading********************************************

    /**
     * Creates the url based on established preferences and uses it for API call to update
     * the search results
     */
    @Override
    public Loader<Location> onCreateLoader(int i, Bundle bundle) {

        // Create a new loader for the given URL
        Uri baseUri = Uri.parse(LOCATIONS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

// https://api.skypicker.com/locations?type=dump&locale=en-US&location_types=airport&
// location_types=city&location_types=country&limit=10&sort=name&active_only=true
        uriBuilder.appendQueryParameter("type", "dump");
        uriBuilder.appendQueryParameter("locale", langLocale);
        uriBuilder.appendQueryParameter("location_types", "airport");
        uriBuilder.appendQueryParameter("limit", "10000");
        uriBuilder.appendQueryParameter("sort", "name");
        uriBuilder.appendQueryParameter("active_only", "true");


        String finalLocURI = uriBuilder.toString();
        Log.e(LOG_TAG, "Final loc url generated: " + finalLocURI);

        return new LocationLoader(this, finalLocURI);
    }

    @Override
    public void onLoadFinished(Loader<Location> lLoader, Location locationStrings) {

        // Clear the adapter of previous earthquake data
        adapterFrom.clear();
        adapterTo.clear();
        //todo: what if the call doesn't work and throw null pointer exception?
        locsList = locationStrings.getLocsList();
        locsMap = locationStrings.getLocsMap();
        Log.e(LOG_TAG, "Locs map" + locsMap);

        // If there is a valid list of {@link Trip}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (locsList != null && !locsList.isEmpty()) {

            Collections.sort(locsList);
            adapterFrom.addAll(locsList);
            adapterTo.addAll(locsList);
            adapterTo.add("anywhere");
        }

      //  else{
      //      adapterFrom.addAll(locFrom);
      //  }
    }

    @Override
    public void onLoaderReset(Loader<Location> loader) {
        // Loader reset, so we can clear out our existing data.
        adapterFrom.clear();
        adapterTo.clear();
    }


}

//if(TextUtils.isEmpty(nameText.getText()))
//    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
