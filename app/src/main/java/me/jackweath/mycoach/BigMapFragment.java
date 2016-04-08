package me.jackweath.mycoach;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by jackweatherilt on 31/03/16.
 */
public class BigMapFragment extends SupportMapFragment {

    public BigMapFragment() {
        super();
    }

    public static BigMapFragment newInstance(){
        BigMapFragment fragment = new BigMapFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        Fragment fragment = getParentFragment();


        if (fragment != null && fragment instanceof OnMapReadyListener) {
            Log.d("MAP_DEBUG", "onMapReady setup");
            ((OnMapReadyListener) fragment).onMapReady();
        }

        view.setClickable(true);
        view.setEnabled(true);


        return view;
    }

    public static interface OnMapReadyListener {
        void onMapReady();
    }
}
