package me.jackweath.mycoach;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class DetailFragment extends Fragment {

    ViewPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;


    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_details, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        adapter = new ViewPagerAdapter(getChildFragmentManager());

        GraphFragment speed = GraphFragment.newInstance("Time", "Speed", randomPoints(7));
        GraphFragment caden = GraphFragment.newInstance("Time", "Distance", randomPoints(8));
        GraphFragment stride = GraphFragment.newInstance("Time", "Distance", randomPoints(9));
        GraphFragment eleva = GraphFragment.newInstance("Time", "Distance", randomPoints(25));

        adapter.addFragment(speed, "Speed");
        adapter.addFragment(caden, "Cadence");
        adapter.addFragment(stride, "Stride Length");
        adapter.addFragment(eleva, "Elevation");


        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private List<PointValue> randomPoints(int length) {
        List<PointValue> points = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < length; i++) {
            points.add(new PointValue(i, rand.nextInt(length*2)));
        }

        return points;
    }

}
