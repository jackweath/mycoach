package me.jackweath.mycoach;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

public class PreRunActivity extends AppCompatActivity {

    RadioButton openMode, sprintMode, paceMode;
    String mode = null;
    int level = 1;
    boolean stretch, reducedAudio, autoLevel;
    ViewPagerAdapter adapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        Button bottomBtn = (Button) findViewById(R.id.bottomBtn);
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                beginRun();
            }
        });
        bottomBtn.setText("Begin run");

        dotDisplaySetup();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        ModeCardFragment open = ModeCardFragment.newInstance(
                getString(R.string.open_title),
                getString(R.string.open_desc),
                getString(R.string.open_mode));
        ModeCardFragment pace = ModeCardFragment.newInstance(
                getString(R.string.pace_title),
                getString(R.string.pace_desc),
                getString(R.string.pace_mode));
        ModeCardFragment sprint = ModeCardFragment.newInstance(
                getString(R.string.sprint_title),
                getString(R.string.sprint_desc),
                getString(R.string.sprint_mode));

        adapter.addFragment(open, open.mMode);
        adapter.addFragment(pace, pace.mMode);
        adapter.addFragment(sprint, sprint.mMode);
        viewPager.setAdapter(adapter);
    }

    public void dotDisplaySetup() {
        final ImageView dot0 = (ImageView) findViewById(R.id.dot0);
        final ImageView dot1 = (ImageView) findViewById(R.id.dot1);
        final ImageView dot2 = (ImageView) findViewById(R.id.dot2);

        // Setup initial dots
        dot0.setImageResource(R.drawable.ic_dot_8dp);
        dot1.setImageResource(R.drawable.ic_dot_unselected);
        dot2.setImageResource(R.drawable.ic_dot_unselected);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch(position) {
                    case 0:
                        dot0.setImageResource(R.drawable.ic_dot_8dp);
                        dot1.setImageResource(R.drawable.ic_dot_unselected);
                        dot2.setImageResource(R.drawable.ic_dot_unselected);
                        break;
                    case 1:
                        dot0.setImageResource(R.drawable.ic_dot_unselected);
                        dot1.setImageResource(R.drawable.ic_dot_8dp);
                        dot2.setImageResource(R.drawable.ic_dot_unselected);
                        break;
                    case 2:
                        dot0.setImageResource(R.drawable.ic_dot_unselected);
                        dot1.setImageResource(R.drawable.ic_dot_unselected);
                        dot2.setImageResource(R.drawable.ic_dot_8dp);
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    public void beginRun() {
        mode = adapter.getPageTitle(viewPager.getCurrentItem()).toString();
        stretch = ((Switch) findViewById(R.id.stretchSwitch)).isChecked();
        reducedAudio = ((Switch) findViewById(R.id.reducedAudioSwitch)).isChecked();
        autoLevel = ((Switch) findViewById(R.id.autoLevelSwitch)).isChecked();


        if (mode != null) {
            Intent intent = new Intent(getApplicationContext(), InRun.class);
            /* Send details :
                * Starting level
                * Mode
                * Stretch option
                * Reduced audio option
                * Auto level adjustments option
            */
            intent.putExtra("mode", mode);
            intent.putExtra("level", level);
            intent.putExtra("stretch", stretch);
            intent.putExtra("reducedAudio", reducedAudio);
            intent.putExtra("autoLevel", autoLevel);

            startActivity(intent);

            finish();

        } else {
            // Tell them to select a mode
            new AlertDialog.Builder(PreRunActivity.this)
                    .setTitle("Choose a mode")
                    .setMessage("To start running, you need to choose a running mode.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing?
                        }
                    })
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .show();
        }
    }

}
