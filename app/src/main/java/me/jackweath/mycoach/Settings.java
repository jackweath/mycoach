package me.jackweath.mycoach;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends AppCompatActivity {

    EditText nameText, dobText, heightText, weightText;
    Spinner genderSpinner;
    SharedPreferences sharedPref;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Personal settings");
        sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.users_settings), Context.MODE_PRIVATE);


        Spinner genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_types, android.R.layout.simple_spinner_item);

        Button bottomBtn = (Button) findViewById(R.id.bottomBtn);

        selectFields();
        loadSettings();

        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveSettings();
                finish();
            }
        });
        bottomBtn.setText("Update settings");

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        genderSpinner.setAdapter(adapter);
    }

    private void saveSettings() {
        String name = nameText.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String dob = dobText.getText().toString();
        int height = Integer.valueOf(heightText.getText().toString());
        float weight = Float.valueOf(weightText.getText().toString());

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(getString(R.string.users_name), name);
        editor.putString(getString(R.string.users_gender), gender);
        editor.putString(getString(R.string.users_dob), dob);
        editor.putInt(getString(R.string.users_height), height);
        editor.putFloat(getString(R.string.users_weight), weight);

        editor.commit();
    }

    private void loadSettings() {
        String name = sharedPref.getString(getString(R.string.users_name), "");
        String gender = sharedPref.getString(getString(R.string.users_gender), "");
        String dob = sharedPref.getString(getString(R.string.users_dob), "");
        int height = sharedPref.getInt(getString(R.string.users_height), -1);
        float weight = sharedPref.getFloat(getString(R.string.users_weight), -1.0f);

        nameText.setText(name);
        genderSpinner.setSelection(adapter.getPosition(gender));
        dobText.setText(dob);

        if (height < 0) {
            // nothing
        } else {
            heightText.setText(String.valueOf(height));
        }

        if (weight < 0) {
            // nothing
        } else {
            weightText.setText(String.valueOf(weight));
        }
    }

    private void selectFields() {
        nameText = (EditText) findViewById(R.id.nameText);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        dobText = (EditText) findViewById(R.id.dobText);
        heightText = (EditText) findViewById(R.id.heightText);
        weightText = (EditText) findViewById(R.id.weightText);
    }

    public static Map<String, String> getUserDetails(Context context) {
        Map<String, String> settings = new HashMap<>();

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.users_settings), Context.MODE_PRIVATE);

        int[] ids = {R.string.users_name,
                        R.string.users_dob,
                        R.string.users_gender,
                        R.string.users_height,
                        R.string.users_weight};

        String val;

        for (int id : ids) {
            String key = context.getString(id);
            try {
                 val = sharedPref.getString(key, "");
            } catch(RuntimeException e) {
                try {
                    val = String.valueOf(sharedPref.getInt(key, -1));
                } catch (RuntimeException f){
                    val = String.valueOf(sharedPref.getFloat(key, -1.0f));
                }
            }

            settings.put(key, val);

        }

        return settings;
    }
}
