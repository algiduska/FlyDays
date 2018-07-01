package com.example.android.flydays;

import java.util.List;

public class Trip {
    private int price;
    private String currency;
    private String bookingURL;
    private boolean oneway; //will be true or false to indicate type of trip - should be passed by search button
    private String depCity;
    private String arrCity;
    //durations in seconds
    private long depDuration;
    private long retDuration;
    private List<Flight> flights;


    public Trip(List<Flight> flights, int price, String currency, String bookingURL,
                String depCity, String arrCity, long depDuration, long retDuration){
        this.price=price;
        this.currency=currency;
        this.bookingURL=bookingURL;
        this.oneway = retDuration==0;
        this.depCity=depCity;
        this.arrCity=arrCity;
        this.depDuration = depDuration;
        this.retDuration = retDuration;
        this.flights=flights;
    }

    public int getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBookingURL() {
        return bookingURL;
    }

    public boolean isOneway() {
        return oneway;
    }

    public String getDepCity() {
        return depCity;
    }

    public String getArrCity() {
        return arrCity;
    }

    public long getDepDuration() {
        return depDuration;
    }

    public long getRetDuration() {
        return retDuration;
    }

    public List<Flight> getFlights() {
        return flights;
    }
}

