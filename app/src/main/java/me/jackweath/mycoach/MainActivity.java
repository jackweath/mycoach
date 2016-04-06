package me.jackweath.mycoach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("myCoach");
        getSupportActionBar().setIcon(R.drawable.icon_pad);

        Button bottomBtn = (Button) findViewById(R.id.bottomBtn);
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openPreRun();
            }
        });
        bottomBtn.setText("Start run");

        refreshDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Open settings
                intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                break;
            case R.id.action_data:
                // Open settings
                intent = new Intent(getApplicationContext(), DataSettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openPreRun() {
        Intent intent = new Intent(getApplicationContext(), PreRunActivity.class);
        startActivity(intent);
    }

    private void refreshDetails() {
        Map<String, String> userDetails = Settings.getUserDetails(getApplicationContext());

        getSupportActionBar().setSubtitle(
                userDetails.get(getString(R.string.users_name)));

    }
}
