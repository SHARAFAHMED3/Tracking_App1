package com.example.appintroduction;

public class MyLocation {
    private  double Longitude;
    private double Latitude;


    public MyLocation(double longitude, double latitude) {
        Longitude = longitude;
        Latitude = latitude;
    }
    public MyLocation() {
        // Empty constructor required for Firebase deserialization
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }
}
