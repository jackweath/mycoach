package me.jackweath.mycoach;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class CustomLocation {

    private double latitude;
    private double longitude;
    private Double elevation;

    public CustomLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public CustomLocation(String locationString) {
        locationString = locationString.replace("[", "").replace("]", "");
        String latLngDivider = ":";
        String[] latLng = locationString.split(latLngDivider);

        if (latLng.length != 2) {
            Log.e("LOCAT_PARSE", "Incorrect location format given.");
        } else {
            longitude = Double.parseDouble(latLng[0]);
            latitude = Double.parseDouble(latLng[1]);
        }
    }

    public Double getElevation() {
        if (elevation == null || elevation == Double.NaN) {
            Log.d("ELEVATION", "About to start thread...");
            Thread thread = new Thread() {
                @Override
                public void run() {
                    Log.i("ElevationThread", "Thread is running");
                    String urlStr = "http://maps.googleapis.com/maps/api/elevation/"
                            + "xml?locations=" + String.valueOf(latitude)
                            + "," + String.valueOf(longitude)
                            + "&sensor=true";

                    try {
                        URL url = new URL(urlStr);

                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(url.openStream());
                        doc.getDocumentElement().normalize();
                        NodeList nList = doc.getElementsByTagName("elevation");

                        elevation =  Double.parseDouble(nList.item(0).getTextContent());
                    } catch(Exception e) {
                        e.printStackTrace();

                        elevation = Double.NaN;
                    }
                    Log.i("ElevationThread", "Thread is closing");
                }
            };

            thread.start();
            try {
                thread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }


        Log.d("ELEVATION", elevation + "");
        return elevation;
    }

    // Returns string format (that can be parsed back later), for database storage
    public String toString() {
        return "[" + latitude + ":" + longitude + "]";
    }

    // Returns an LatLng object, needed for the polyline plotting on google maps
    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
