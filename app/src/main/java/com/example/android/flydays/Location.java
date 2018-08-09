package com.example.android.flydays;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Location implements Serializable{

    private ArrayList<String> locsList;
    private Map locsMap;
    private HashMap airoportMap;

    //todo: edit in report -- added airportMap to Location object to find airport name based on code (for summary dialog) - could be done by locs map but there the order is other way around

    public Location(ArrayList<String> locsList, Map locsMap, HashMap airportMap){
        this.locsList=locsList;
        this.locsMap=locsMap;
        this.airoportMap=airportMap;
    }

    public Map getLocsMap() {
        return locsMap;
    }

    public ArrayList<String> getLocsList() {
        return locsList;
    }

    public HashMap getAirportMap() {
        return airoportMap;
    }
}

/*
public class Location {
    private String airCode;
    private String airName;
    private String cityCode;
    private String cityName;
    private String countryCode;
    private String countryName;
    //private String locType; -- not needed as they will all be airports
    private String timezone;

    public Location(String airCode, String airName, String cityCode, String cityName, String countryCode,
                    String countryName, String timezone){
        this.airCode=airCode;
        this.airName=airName;
        this.cityCode=cityCode;
        this.cityName=cityName;
        this.countryCode=countryCode;
        this.countryName=countryName;
        this.timezone=timezone;
    }

    public String getAirCode() {
        return airCode;
    }

    public String getAirName() {
        return airName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getTimezone() {
        return timezone;
    }
}
*/