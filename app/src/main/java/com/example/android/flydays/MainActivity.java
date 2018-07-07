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
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;

import android.app.TimePickerDialog;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
//todo: edit trip activity to show one way flights correctly
//todo: make it work with range
//todo: create an introduction page (with logo) for location api to be called
//todo: add extra filtering options
//todo: create a custom adapter? to sort locations better
    //https://stackoverflow.com/questions/11574752/autocompletetextview-doesnt-suggest-what-i-want


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoaderCallbacks<Location>{

    MultiAutoCompleteTextView locFromView;
    MultiAutoCompleteTextView locToView;
    EditText depDateText;
    EditText retDateText;
    CheckBox dirOnlyBox;
    CheckBox oneWayOnly;
    EditText daysInText;
    Button searchButton;

    String langLocale;

    private int dYear, dMonth, dDay;

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


        depDateText = findViewById(R.id.departure_date);
        retDateText = findViewById(R.id.return_date);

        depDateText.setOnClickListener(this);
        retDateText.setOnClickListener(this);

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
        //Log.e(LOG_TAG, "calendar c after creation" + c);


        //buttons for days, xml and code from https://stackoverflow.com/questions/32534076/what-is-the-best-way-to-do-a-button-group-that-can-be-selected-and-activate-inde
        for(int i = 0; i < btn.length; i++){
            btn[i] = (Button) findViewById(btn_id[i]);
            btn[i].setBackgroundColor(Color.rgb(207, 207, 207));
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
        locToView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        locFromView.setThreshold(1);
        locFromView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
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
        depDateText.getText().clear();
        retDateText.getText().clear();
        dirOnlyBox.toggle();


    }


//**************************************************************************************************
    @Override
    public void onClick(View view) {


        //using switch to get the right onClick implementation for buttons
        switch (view.getId()) {

            case R.id.search_button:
                Intent search = new Intent(MainActivity.this, TripActivity.class);

                //on how to pass data:
                //https://stackoverflow.com/questions/3510649/how-to-pass-a-value-from-one-activity-to-another-in-android
                Bundle bundle = new Bundle();

                //Add your data to bundle
                bundle.putString("dloc", (locsMap.get((locFromView.getText()).toString())).toString());
                bundle.putString("hloc", (locsMap.get((locToView.getText()).toString())).toString());
                bundle.putString("ddate", (depDateText.getText()).toString());
                //todo: make a toast message if rdate is before ddate
                bundle.putString("rdate", (retDateText.getText()).toString());
                bundle.putString("days", daysInText.getText().toString());
                bundle.putBoolean("dir", dirOnlyBox.isChecked());
                bundle.putBoolean("ow", oneWayOnly.isChecked());
                bundle.putString("lang", langLocale);

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
                                depDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, dYear, dMonth, dDay);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

                depDateText.setFocusable(false);
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
                                retDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

                            }
                        }, dYear, dMonth, dDay);

                datePickerDialog2.getDatePicker().setMinDate(c.getTimeInMillis());

                retDateText.setFocusable(false);
                datePickerDialog2.setTitle("");
                datePickerDialog2.show();
                break;
            case R.id.one_way :
                if(oneWayOnly.isChecked())
                    daysInText.setText("oneway", TextView.BufferType.EDITABLE);
                break;
            case R.id.btn0 :
                setFocus(btn_unfocus, btn[0]);
                break;

            case R.id.btn1 :
                setFocus(btn_unfocus, btn[1]);
                break;

            case R.id.btn2 :
                setFocus(btn_unfocus, btn[2]);
                break;

            case R.id.btn3 :
                setFocus(btn_unfocus, btn[3]);
                break;

            case R.id.btn4 :
                setFocus(btn_unfocus, btn[4]);
                break;

            case R.id.btn5 :
                setFocus(btn_unfocus, btn[5]);
                break;

            case R.id.btn6 :
                setFocus(btn_unfocus, btn[6]);
                break;
            default:
                Log.e(LOG_TAG, "OnClick went to default");
                break;
        }
    }

    private void setFocus(Button btn_unfocus, Button btn_focus){
        btn_unfocus.setTextColor(Color.rgb(49, 50, 51));
        btn_unfocus.setBackgroundColor(Color.rgb(207, 207, 207));
        btn_focus.setTextColor(Color.rgb(255, 255, 255));
        btn_focus.setBackgroundColor(Color.rgb(3, 106, 150));
        this.btn_unfocus = btn_focus;
    }

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
