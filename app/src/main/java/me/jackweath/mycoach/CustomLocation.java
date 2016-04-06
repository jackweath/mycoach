package me.jackweath.mycoach;

import android.location.Location;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by jackweatherilt on 05/04/16.
 */
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

    public double getElevation() {
        if (elevation != null) {
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

                return 0.0;
            }
        }

        return elevation;
    }

    public String toString() {
        return "[" + latitude + ":" + latitude + "]";
    }
}
