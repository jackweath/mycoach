package me.jackweath.mycoach;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by jackweatherilt on 03/04/16.
 */
public class DataSettingsActivity extends AppCompatActivity {

    private DataManage dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Data settings");

        dbHelper = new DataManage(getApplicationContext());

        Button deleteDataBtn = (Button) findViewById(R.id.deleteDataBtn);
        deleteDataBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteAllData();
            }
        });

        Button bottomBtn = (Button) findViewById(R.id.bottomBtn);
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        bottomBtn.setText("Done");
    }


    private void deleteAllData() {
        dbHelper.deleteAll();

        Toast.makeText(DataSettingsActivity.this, "All data has been deleted", Toast.LENGTH_LONG).show();
    }
}
