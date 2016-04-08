package me.jackweath.mycoach;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.LocationListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class InRun extends AppCompatActivity implements SensorEventListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    Location locat, prevLocat;

    // Screen elements to hide when on open
    int[] openHide = {
            R.id.targetBtn,
            R.id.intervalCard,
            R.id.levelCard,
            R.id.targetPercCard,
            R.id.levelAdjustSwitch };
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    String mode;
    int level;
    boolean stretch, reducedAudio, autoLevel;
    boolean initial;
    boolean valuesExist;

    int steps, prevSteps;
    double distance;
    Calc.ValStat speedMK, speedMS, stride, cadence;
    double calories;
    double percVal;

    HashMap<String, Double> targets;

    boolean permissionsComplete, connected;
    boolean paused;
    int prevInterval;

    SensorManager sensorManager;
    Sensor countSensor;

    DataManage dbHelper;
    Mode modeLevel;
    int interval;
    double weight;
    long startTime, runTime;
    long lastInsert;
    int rowsUsed;

    Timer timer;
    Button bottomBtn;
    ProgressDialog progress;
    boolean loadingPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_run);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        permissionConfig();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Get passed settings
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            // An error has occured if this is executed
            Log.e("InRunExtras", "There were no run options found!");
        } else {
            // Pass over the settings!
            mode = extras.getString("mode");
            level = extras.getInt("level");
            stretch = extras.getBoolean("stretch");
            reducedAudio = extras.getBoolean("reducedAudio");
            autoLevel = extras.getBoolean("autoLevel");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle();

        bottomBtn = (Button) findViewById(R.id.bottomBtn);
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pauseRun();
            }
        });
        bottomBtn.setText("Pause run");

        // Get user's weight
        weight = Double.parseDouble(
                Settings.getUserDetails(getApplicationContext())
                        .get(getString(R.string.users_weight)));

        progress = new ProgressDialog(this);
        progress.setTitle("Fixing your GPS position...");
        progress.setMessage("This usually takes around 15 seconds.");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();
        loadingPresent = true;
    }

    private void startRun() {
        initial = true;
        distance = 0.0;
        paused = false;
        calories = 0;
        speedMK = null;
        speedMS = null;
        cadence = null;
        stride = null;
        interval = 1;
        dbHelper = new DataManage(getApplicationContext());
        dbHelper.freshLiveData();
        modeLevel = new Mode(level, mode);
        targets = modeLevel.targets;
        steps = 0;

        if (!mode.equals(getString(R.string.open_mode))) {
            ((TextView) findViewById(R.id.levelText))
                    .setText(String.valueOf(modeLevel.level));
            ((TextView) findViewById(R.id.intText))
                    .setText(interval + " of " +
                            Math.round(targets.get("intervals")));
        }


        timer = new Timer();

        rowsUsed = 10;
        int dataInsertRate = 500;
        timer.schedule(insertData, 100, dataInsertRate);
        timer.schedule(secondaryCalcs, 100, dataInsertRate * rowsUsed);
        timer.schedule(updateUI, 0, 500);

        startTime = Calc.timeMilli();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_in_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.end_run:
                endRunPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    TimerTask insertData = new TimerTask(){
        @Override
        public void run(){
            runTime = Calc.timeMilli() - startTime;
            if (prevLocat != null) {

                /* === NEW CODE HERE === */
                CustomLocation locatFormat = new CustomLocation(locat);

                lastInsert = dbHelper.addLiveData(runTime, steps,
                        interval, distance, locatFormat.toString(), level);
            }
        }
    };

    TimerTask secondaryCalcs = new TimerTask() {
        @Override
        public void run(){
            if (lastInsert > rowsUsed + 1) {
                // Cadence is not recorded in live data, but displayed
                // Stride length is not recorded in live data, but displayed
                // Speed is not recorded in live data, but displayed

                double distanceTaken = 0.0;
                int stepsTaken = 0;
                long timeTaken = 0;

                ArrayList<String> row = dbHelper.readTableRow("live", lastInsert);

                // Take the time
                timeTaken += Long.parseLong(row.get(1));
                // Take the distance
                distanceTaken += Double.parseDouble(row.get(4));
                // Take the steps
                stepsTaken += Integer.parseInt(row.get(2));

                row = dbHelper.readTableRow("live", lastInsert - rowsUsed);

                // Take the time
                timeTaken -= Long.parseLong(row.get(1));
                // Take the distance
                distanceTaken -= Double.parseDouble(row.get(4));
                // Take the steps
                stepsTaken -= Double.parseDouble(row.get(2));

                stride = Calc.strideLength(distanceTaken, stepsTaken);
                cadence = Calc.cadence(stepsTaken, timeTaken);
                speedMK = Calc.speedMK(distanceTaken, timeTaken);
                speedMS = Calc.speedMS(distanceTaken, timeTaken);


                calories += Calc.calories(speedMS, timeTaken, weight);
                Log.d("CAL_DEBUG", calories +" MAIN THREAD");

                checkTargetPerc();

                valuesExist = true;
            }
        }
    };

    TimerTask updateUI = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (valuesExist) {
                        TextView distText = (TextView) findViewById(R.id.distText);
                        TextView speedText = (TextView) findViewById(R.id.speedText);
                        TextView calText = (TextView) findViewById(R.id.calText);
                        TextView percText = (TextView) findViewById(R.id.percText);
                        TextView strideText = (TextView) findViewById(R.id.strideText);
                        TextView cadenText = (TextView) findViewById(R.id.cadenText);
                        TextView timeText = (TextView) findViewById(R.id.timeText);
                        ImageView percIcon = (ImageView) findViewById(R.id.percIcon);

                        Calc.ValStat distValStat = new Calc.ValStat(distance, false);

                        distText.setText(distValStat.roundDp(1, true) + " km");
                        speedText.setText(speedMK.roundDp(1) + " min/km");
                        calText.setText(Math.round(calories) + " cal");
                        percText.setText(Math.round(percVal) + "%");
                        strideText.setText(stride.round() + " m");
                        cadenText.setText(cadence.round() + " steps/min");
                        timeText.setText(Calc.formatTime(runTime));


                        if (mode.equals("open")) {
                            percIcon.setImageResource(R.drawable.cool);
                        } else {
                            if (percVal <= 90.0) {
                                percIcon.setImageResource(R.drawable.bad);
                            } else if (percVal < 97.5 && percVal > 90.0) {
                                percIcon.setImageResource(R.drawable.borderline);
                            } else if (percVal >= 97.5 && percVal < 102.5) {
                                percIcon.setImageResource(R.drawable.good);
                            }
                            if (percVal >= 102.5) {
                                percIcon.setImageResource(R.drawable.vgood);
                            }
                        }

                        if (loadingPresent) {
                            progress.dismiss();
                            loadingPresent = false;
                        }
                    }

                }
            });
        }
    };

    private void setTitle() {
        String title;
        switch (mode.toLowerCase()) {
            case "open":
                title = getString(R.string.open_title);
                break;
            case "pace":
                title = getString(R.string.pace_title);
                break;
            case "sprint":
                title = getString(R.string.sprint_title);
                break;
            default:
                title = "Unknown mode";
        }

        getSupportActionBar().setTitle(title);
    }

    private void checkTargetPerc() {
        HashMap<String, Double> userVals = new HashMap<>();

        userVals.clear();
        userVals.put("calories", calories);
        userVals.put("speed", speedMK.value.doubleValue());
        userVals.put("cadence", cadence.value.doubleValue());
        userVals.put("stride", stride.value.doubleValue());

        percVal = modeLevel.compareAll(userVals);
    }

    /* ********************************************* */
    /*               User interaction                */
    public void endRunPressed() {
        // Pause run (for the duration of the pop up)
        paused = true;

        // Confirmation pop-up
        new AlertDialog.Builder(InRun.this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure that you want to end your run?")
                .setPositiveButton("End run", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // End run tasks
                        timer.cancel();

                        // Getting a neat format for the calendar
                        Calendar date = GregorianCalendar.getInstance();
                        String runTitle = mode + " "
                                + date.get(Calendar.DAY_OF_MONTH) + "/"
                                + date.get(Calendar.MONTH) + "/"
                                + date.get(Calendar.YEAR) + " "
                                + date.get(Calendar.HOUR_OF_DAY) + ":"
                                + date.get(Calendar.MINUTE) ;

                        // Get the time since epoch (used for the date)
                        long timeEpoch = System.currentTimeMillis();
                        Log.d("TitleMischief", runTitle);
                        // Data sent to summary table & detail table
                        long runID = dbHelper.addSummaryData(runTitle, mode, interval, timeEpoch,
                                distance, runTime, steps, level);

                        dbHelper.processData(lastInsert, runID); // Relational database!

                        // Open post-run activity.
                        Intent intent = new Intent(getApplicationContext(), PostRunActivity.class);
                        intent.putExtra("runID", runID); // Give the next activity the run ID
                        startActivity(intent);

                        // Stop listeners
                        stopListening();

                        // Close activity.
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, return back to the run
                        paused = false;
                    }
                })
                .setIcon(R.drawable.ic_warning_black_24dp)
                .show();
    }

    public void viewTargsPressed(View view) {
        // Confirmation pop-up
        String targText = "";

        Iterator targIter = targets.entrySet().iterator();

        while (targIter.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) targIter.next();
            targText += pair.getKey().toString().toUpperCase() + ": " + pair.getValue().toString() + "\n";
        }

        new AlertDialog.Builder(InRun.this)
                .setTitle("Your targets")
                .setMessage(targText)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setIcon(R.drawable.ic_target_notif)
                .show();
    }

    private void pauseRun() {
        // If the run is already paused
        if (paused) {
            // Unpause the run, and carry on
            paused = false;
            // Change the button text back to "pause run"
            bottomBtn.setText("PAUSE RUN");
            interval = prevInterval + 1;
        } else {
            // Record the previous interval
            prevInterval = interval;
            // Pause the run
            paused = true;
            // Change button text to "continue running"
            bottomBtn.setText("CONTINUE RUNNING");
            interval = 0;
        }

        if (!mode.equals(getString(R.string.open_mode))) {
            ((TextView) findViewById(R.id.intText))
                    .setText(interval + " of " +
                            Math.round(targets.get("intervals")));
        }
    }

    public void levelChange(View view) {
        // Checks which button was pressed, and act appropriately
        switch(view.getId()) {
            case R.id.lowerBtn:
                // Users cannot go below level 1
                if (level > 1) {
                    level --;
                } else {
                    // Notifiying the user through toast
                    Toast.makeText(InRun.this, "Level 1 is the minumum level.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.raiseBtn:
                // Users cannot exceed level 50
                if (level < 50) {
                    level ++;
                } else {
                    // Notifying the user through toast
                    Toast.makeText(InRun.this, "Level 50 is the maximum level.", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        // new modeLevel object created
        modeLevel = new Mode(level, mode);
        targets = modeLevel.targets;
        ((TextView) findViewById(R.id.levelText))
                .setText(String.valueOf(modeLevel.level));
    }

    /* ********************************************* */
    /*          Sensor & Location management         */
    final int PERMISSION_GIVEN_BOTH_CODE = 200;
    final int PERMISSION_GIVEN_LOCAT_CODE = 201;
    final int PERMISSION_GIVEN_BODY_CODE = 202;

    boolean bodySensorsOkay, locatOkay;

    private void permissionConfig() {
        bodySensorsOkay = true;
        locatOkay = true;

        int fineLocatPermission = ContextCompat.checkSelfPermission(InRun.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int bodySensorPermission = ContextCompat.checkSelfPermission(InRun.this,
                Manifest.permission.BODY_SENSORS);

        int permissionGiven = PackageManager.PERMISSION_GRANTED;

        if (fineLocatPermission != permissionGiven &&
                bodySensorPermission != permissionGiven) {
            // If permission is not granted for both

            ActivityCompat.requestPermissions(InRun.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BODY_SENSORS},
                    PERMISSION_GIVEN_BOTH_CODE);

        } else if (fineLocatPermission != permissionGiven) {
            // If permission is just not given to fine location tracking
            ActivityCompat.requestPermissions(InRun.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_GIVEN_LOCAT_CODE);

        } else if (bodySensorPermission != permissionGiven) {
            // If permission is given to fine location but NOT to body sensor info
            ActivityCompat.requestPermissions(InRun.this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERMISSION_GIVEN_BODY_CODE);
        } else {
            // Otherwise, we have the permissions needed!
            bodySensorsOkay = true;
            locatOkay = true;
            permissionsComplete = true;
            if (connected) {
                setupListeners();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GIVEN_BOTH_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission given!
                } else {
                    // The permission was not given :(
                    locatOkay = false;
                    bodySensorsOkay = false;
                }
                return;
            }
            case PERMISSION_GIVEN_LOCAT_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission given!
                } else {
                    // The permission was not given :(
                    locatOkay = false;
                }
                return;
            }
            case PERMISSION_GIVEN_BODY_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission given!
                } else {
                    // The permission was not given :(
                    bodySensorsOkay = false;
                }
                return;
            }
        }

        if (countSensor == null) {
            bodySensorsOkay = false;
        }

        permissionsComplete = true;
        if (connected) {
            setupListeners();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    private void stopListening() {
        mGoogleApiClient.disconnect();
        sensorManager.unregisterListener(InRun.this, countSensor);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(800);
        mLocationRequest.setSmallestDisplacement(2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            status.startResolutionForResult(
                                    InRun.this,
                                    1); // Not sure what to do here
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    // When connected to the Google API...
    @Override
    public void onConnected(Bundle connectionHint) {
        connected = true;

        if (permissionsComplete) {
            setupListeners();
        }

        Log.d("INRUN", "Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int val) {}
    @Override
    public void onConnectionFailed(ConnectionResult result) {}

    @Override
    public void onLocationChanged(Location location) {
        locat = location;

        if (prevLocat != null) {
            // Calculate the distance!
            distance += Calc.distance(prevLocat, locat);
        }

        Log.d("DIST_DEBUG", locat.toString());
        Log.d("DIST_DEBUG", String.valueOf(distance));

        prevLocat = locat;
    }

    private void setupListeners() {
        Log.d("INRUN", "Locat okay:" + locatOkay +" | bodyOkay:" + bodySensorsOkay);
        if (locatOkay) {
            createLocationRequest();
            Log.d("INRUN", "Requested location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

            startRun();
        }

        if (bodySensorsOkay) {
            sensorManager.registerListener(InRun.this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (initial) {
            prevSteps = (int)(event.values[0]);
            initial = false;
        } else {
            steps = (int)(event.values[0]) - prevSteps;       // Get the step count, convert to integer
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int val ) {

    }
}
