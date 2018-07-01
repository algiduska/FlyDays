package com.example.android.flydays;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    EditText depLocText;
    EditText holLocText;
    EditText depDateText;
    EditText retDateText;
    CheckBox dirOnlyBox;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        depLocText = findViewById(R.id.location_from);
        holLocText = findViewById(R.id.location_to);

        depDateText = findViewById(R.id.departure_date);
        retDateText = findViewById(R.id.return_date);

        dirOnlyBox = findViewById(R.id.direct_checkbox);

        final Button searchButton = findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                Intent search = new Intent(MainActivity.this, TripActivity.class);

                //on how to pass data:
                //https://stackoverflow.com/questions/3510649/how-to-pass-a-value-from-one-activity-to-another-in-android
                Bundle bundle = new Bundle();

                //Add your data to bundle
                bundle.putString("dloc", (depLocText.getText()).toString());
                bundle.putString("hloc", (holLocText.getText()).toString());
                bundle.putString("ddate", (depDateText.getText()).toString());
                bundle.putString("rdate", (retDateText.getText()).toString());
                bundle.putBoolean("dir", dirOnlyBox.isChecked());

                //Add the bundle to the intent
                search.putExtras(bundle);

                startActivity(search);
            }
        });

        //todo: make an api call that would populate the strings.xml with airports, cities and countries

        // what is needed for autoCompleteTextView: https://developer.android.com/training/keyboard-input/style

        // Get a reference to the AutoCompleteTextView in the layout
        AutoCompleteTextView locFromView = (AutoCompleteTextView) findViewById(R.id.location_from);
        AutoCompleteTextView locToView = (AutoCompleteTextView) findViewById(R.id.location_to);
        // Get the string array
        String[] locFrom = getResources().getStringArray(R.array.from_options);
        String[] locTo = getResources().getStringArray(R.array.hol_options);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapterFrom =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locFrom);
        ArrayAdapter<String> adapterTo =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, locTo);

        locFromView.setAdapter(adapterFrom);
        locToView.setAdapter(adapterTo);

        //todo: go directly to the next field when finished with one
        //similar example: https://stackoverflow.com/questions/23123833/edittext-automatically-go-to-a-new-line


    }



    //depLocText.getText().clear();
}

    //if(TextUtils.isEmpty(nameText.getText()))
    //    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();

