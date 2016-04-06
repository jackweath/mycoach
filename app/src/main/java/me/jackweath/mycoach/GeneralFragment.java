package me.jackweath.mycoach;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.GenericArrayType;


/**
 * Created by jackweatherilt on 30/03/16.
 */
public class GeneralFragment extends Fragment implements OnMapReadyCallback {

    BigMapFragment mMapFragment;
    private static GoogleMap mMap;
    View view;
    private String tlTitle, tlText,
                    blTitle, blText,
                    trTitle, trText,
                    brTitle, brText;
    boolean cardsVisible = true;
    Button showCardsBtn, fullscreenBtn;
    LinearLayout cardHolder;

    public GeneralFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_general, container, false);
        mMapFragment = BigMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.mapContainer, mMapFragment).commit();

        // Temporary placeholders inserted
        ((TextView) view.findViewById(R.id.tlText)).setText(tlText);
        ((TextView) view.findViewById(R.id.trText)).setText(trText);
        ((TextView) view.findViewById(R.id.tlTitle)).setText(tlTitle);
        ((TextView) view.findViewById(R.id.trTitle)).setText(trTitle);
        ((TextView) view.findViewById(R.id.brText)).setText(brText);
        ((TextView) view.findViewById(R.id.blText)).setText(blText);
        ((TextView) view.findViewById(R.id.blTitle)).setText(blTitle);
        ((TextView) view.findViewById(R.id.brTitle)).setText(brTitle);

        // Set onClickListeners
        cardHolder = (LinearLayout) view.findViewById(R.id.cardHolder);
        fullscreenBtn = (Button) view.findViewById(R.id.fullscreenBtn);
        showCardsBtn = (Button) view.findViewById(R.id.showCardsBtn);

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the cards
                if (cardsVisible) {
                    // Hide the cards
                    cardHolder.setVisibility(View.INVISIBLE);
                    fullscreenBtn.setVisibility(View.GONE);
                    // Show the "show cards" button
                    showCardsBtn.setVisibility(View.VISIBLE);
                } else {
                    // Hide the "show cards" button
                    showCardsBtn.setVisibility(View.GONE);
                    fullscreenBtn.setVisibility(View.VISIBLE);
                    // Show the cards
                    cardHolder.setVisibility(View.VISIBLE);
                }

                cardsVisible = !cardsVisible;
            }
        };

        fullscreenBtn.setOnClickListener(onClick);
        showCardsBtn.setOnClickListener(onClick);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Add a marker to my home and move the camera
        LatLng home = new LatLng(51.6641486, -0.4107860999999957);
        mMap.addMarker(new MarkerOptions().position(home).title("Where myCoach was developed!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5.0f));
    }

    @Override
    public void onDestroyView() {
        Fragment f = getFragmentManager().findFragmentById(mMapFragment.getId());
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapFragment.onLowMemory();
    }

    public static GeneralFragment newInstance() {
        GeneralFragment frag = new GeneralFragment();

        frag.tlTitle = "Achievement";
        frag.tlText = "Congratulations! You ran an average distance at a sub-average pace, you're a real winner. Please use this app again!";
        frag.trTitle = "General";
        frag.trText = "A fairly small amount of text fills this field, but only up to a point, as there must be space below!";
        frag.blTitle = "Route Info";
        frag.blText = "Your route info. it was long, and most likely boring. You finished (allegedly). ";
        frag.brTitle = "Step Data";
        frag.brText = "Lots of steps were taken. Nice one.";

        return frag;
    }

    public static GeneralFragment newItervalInst(int interval) {
        GeneralFragment frag = new GeneralFragment();

        frag.tlTitle = "Achievement";
        frag.tlText = "Congratulations! You achieved 97% of your targets on level 22 in this interval.";
        frag.trTitle = "Interval: " + interval;
        frag.trText = "Interval: " + interval;
        frag.blTitle = "Split Info";
        frag.blText = "Int. time: 12:52 \n Avg. Speed: 4.1 km/h";
        frag.brTitle = "Step Data";
        frag.brText = "Step information will come here etc.";

        return frag;
    }

}
