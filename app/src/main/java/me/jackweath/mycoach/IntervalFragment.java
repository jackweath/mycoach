package me.jackweath.mycoach;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class IntervalFragment extends Fragment {

    ViewPagerAdapter adapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public IntervalFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_interval, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        adapter = new ViewPagerAdapter(getChildFragmentManager());

        int intsDone = 3;
        for (int i = 1; i < intsDone + 1; i++) {
            adapter.addFragment(GeneralFragment.newItervalInst(i),
                    "Interval " + i);
        }

        if (intsDone <= 3) {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }

        viewPager.setAdapter(adapter);


        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
