package me.jackweath.mycoach;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class PostRunActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private long runID;
    private int intervals;
    private DataManage dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_run);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e("PostRunExtras", "No runID handed to activity");
            finish(); // Close the activity, as there's nothing to display
        } else {
            runID = extras.getLong("runID");
        }

        dbHelper = new DataManage(getApplicationContext());

        ArrayList<String> summaryRow = dbHelper.readTableRow("summary", runID);
        intervals = Integer.parseInt(summaryRow.get(3));
        String title = summaryRow.get(1);
        Log.d("TitleMischief", title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Button bottomBtn = (Button) findViewById(R.id.bottomBtn);
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                donePressed();
            }
        });
        bottomBtn.setText("Done");
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(GeneralFragment.newInstance(runID), "GENERAL");
        adapter.addFragment(IntervalFragment.newInstance(runID, intervals), "INTERVALS");
        adapter.addFragment(new DetailFragment(), "DETAILS");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void donePressed() {
        finish();
    }

}
