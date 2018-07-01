package com.example.android.flydays;

public class Flight {

    private String depAirportCode;
    private String arrAirportCode;
    private long depTimeInSeconds;
    private long arrTimeInSeconds;
    private String airlineCode;
    private String flightNo;


    public Flight(String depAirportCode, String arrAirportCode, long depTimeInSeconds,
                  long arrTimeInSeconds, String airlineCode, String flightNo){
        this.depAirportCode=depAirportCode;
        this.arrAirportCode=arrAirportCode;
        this.depTimeInSeconds=depTimeInSeconds;
        this.arrTimeInSeconds=arrTimeInSeconds;
        this.airlineCode=airlineCode;
        this.flightNo=flightNo;

    }

    public String getDepAirportCode() {
        return depAirportCode;
    }


    public String getArrAirportCode() {
        return arrAirportCode;
    }


    public long getDepTimeInSeconds() {
        return depTimeInSeconds;
    }

    public long getArrTimeInSeconds() {
        return arrTimeInSeconds;
    }


    public String getAirlineCode() {
        return airlineCode;
    }

    public String getFlightNo() {
        return flightNo;
    }


}
