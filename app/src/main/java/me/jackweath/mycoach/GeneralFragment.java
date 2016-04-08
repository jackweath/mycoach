package me.jackweath.mycoach;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;


/**
 * Created by jackweatherilt on 30/03/16.
 */
public class GeneralFragment extends Fragment implements BigMapFragment.OnMapReadyListener {

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
    DataManage dbHelper;
    private long runId;
    private Integer interval;

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

        dbHelper = new DataManage(getContext());

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

        slideOut(showCardsBtn, true, true);
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the cards
                if (cardsVisible) {
                    // Hide the cards
                    slideOut(cardHolder, false, false);
                    slideOut(fullscreenBtn, true, false);
                    slideIn(showCardsBtn);
                } else {
                    slideIn(cardHolder);
                    slideIn(fullscreenBtn);
                    slideOut(showCardsBtn, true, false);
                }

                cardsVisible = !cardsVisible;
            }
        };

        fullscreenBtn.setOnClickListener(onClick);
        showCardsBtn.setOnClickListener(onClick);

        return view;
    }

    @Override
    public void onMapReady() {
        mMap = mMapFragment.getMap();
        Log.d("MAP_DEBUG", "onMapReady callled");
        plotRoute();
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

    public static GeneralFragment newInstance(long runId) {
        GeneralFragment frag = new GeneralFragment();

        frag.runId = runId;

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

    public static GeneralFragment newItervalInst(long runId, int interval) {
        GeneralFragment frag = new GeneralFragment();

        frag.runId = runId;
        frag.interval = interval;

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

    private void plotRoute() {
        Log.d("MAP_DEBUG", "route plotting beginning, " + interval);

        if (interval == null && mMap != null) {
            Log.d("MAP_DEBUG", "no interval, beginning plotting");
            PolylineOptions options = new PolylineOptions();
            ArrayList<ArrayList<String>> entries = dbHelper.getRunDetails(runId);

            CustomLocation start = new CustomLocation((entries.get(0)).get(7));
            LatLng startLatLng = start.toLatLng();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11.0f));
            mMap.addMarker(new MarkerOptions()
                    .position(startLatLng)
                    .title("Run start"));

            Log.d("MAP_DEBUG", startLatLng.toString());

            for (ArrayList<String> row : entries) {
                // 7th item is the location string
                CustomLocation locat = new CustomLocation(row.get(7));
                Log.d("MAP_DEBUG", locat.toString());
                options.add(locat.toLatLng());
            }

            mMap.addPolyline(options
                    .color(Color.RED)
                    .width(5)
                    .visible(true)
                    .zIndex(30));

            Log.d("MAP_DEBUG", "route plotting ended");

        }
    }


    private void slideOut(final View view, boolean slidesRight, boolean instant) {
        int slideDist = view.getWidth();
        if (!slidesRight) {
            slideDist =  - slideDist;
        }

        int slideTime = 300;
        if (instant) {
            slideTime = 0;
        }

        view.animate()
                .translationX(slideDist)
                .alpha(0.0f)
                .setDuration(slideTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
    }

    private void slideIn(View view) {
        view.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
    }

}
