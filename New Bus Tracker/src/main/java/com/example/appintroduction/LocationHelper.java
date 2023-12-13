package com.example.appintroduction;

public class LocationHelper {
    private double longitude;
    private double latitude;
    //private String email;

    public LocationHelper(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
       // this.email = email;
    }
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }

    // Constructor


    // Getters and setters
    // ...
}

