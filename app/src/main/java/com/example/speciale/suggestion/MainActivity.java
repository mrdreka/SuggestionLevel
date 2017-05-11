package com.example.speciale.suggestion;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;
    private Sensor senAccelerometerNormal;

    /**     * The desired interval for location updates. Inexact. Updates may be more or less frequent.     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**     * Provides the entry point to Google Play services.     */
    protected GoogleApiClient mGoogleApiClient;
    /**     * Stores parameters for requests to the FusedLocationProviderApi.     */
    protected LocationRequest mLocationRequest;
    /**     * Represents a geographical location.     */
    protected Location mCurrentLocation;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates = false;


    TextView xView,// yView, vView,
            eta , accSamples,infered, xVariance, yVariance, zVariance, totalVariance;
   // List<AccObj> accObjList = new ArrayList<AccObj>();
    Statistics statsEuclid,statsEuclidY, statsEuclidX, statsEuclidZ ;

    private double mean, stdDev, min, max, thresholdSkii, stdDevX, minX,maxX, thresholdSkiiX;
    private double minY, maxY, stdDevY, thresholdSkiiY, stdDevs;
    private double minZ, maxZ, thresholdSkiiZ, stdDevZ;
    double yDifference = 0, xDifference = 0, zDifference = 0;


    private int shortestTurn = 100;
    //longest is calcualted with amount of shortturn, so
    private int longestTurn, longestTurnX, longestTurnY = 0;
    private int countRegulationEuclid, countRegulationX, topsX,  countRegulationY= 0;

    long endTime,initialTime = 0;

    private boolean risingX, fallingX = false;
    private boolean shortestTurnbool = false;
    private boolean LongestTurnboolMissed = false;
    private boolean thresholdExist = false;

    //unfiltered sensor
    ArrayList<Double> listX = new ArrayList<>();
    ArrayList<Double> listY = new ArrayList<>();
    ArrayList<Double> listZ = new ArrayList<>();
    ArrayList<Double> listEuclidNorm = new ArrayList<>();
    //list for second level filter
    ArrayList<Double> listX2 = new ArrayList<>();
    ArrayList<Double> listY2 = new ArrayList<>();
    ArrayList<Double> listZ2 = new ArrayList<>();
    ArrayList<Double> listEuclidNorm2 = new ArrayList<>();
    //for filtered data to threshold
    ArrayList<Double> listXThreshold= new ArrayList<>();
    ArrayList<Double> listYThreshold= new ArrayList<>();
    ArrayList<Double> listZThreshold = new ArrayList<>();
    ArrayList<Double> listEuclidNormThreshold = new ArrayList<>();
    ////for filtered data to sample
    ArrayList<Double> listXSample = new ArrayList<>();
    ArrayList<Double> listYSample = new ArrayList<>();
    ArrayList<Double> listZSample = new ArrayList<>();
    ArrayList<Double> listEuclidNormSample = new ArrayList<>();
    //for approved sample
    ArrayList<Double> sampleXOld = new ArrayList<>();
    ArrayList<Double> sampleYOld = new ArrayList<>();
    ArrayList<Double> sampleZOld = new ArrayList<>();
    ArrayList<Double> sampleEuclidNormOld = new ArrayList<>();
    //first layer of approved
    ArrayList<Double> sampleXNew = new ArrayList<>();
    ArrayList<Double> sampleYNew = new ArrayList<>();
    ArrayList<Double> sampleZNew = new ArrayList<>();
    ArrayList<Double> SampleEuclidNormNew = new ArrayList<>();

    //gyroscope sensor
    ArrayList<Double> listXG = new ArrayList<>();
    ArrayList<Double> listYG = new ArrayList<>();
    ArrayList<Double> listZG = new ArrayList<>();
    ArrayList<Double> listEuclidNormG = new ArrayList<>();
    //list for second level filter
    ArrayList<Double> listX2G = new ArrayList<>();
    ArrayList<Double> listY2G = new ArrayList<>();
    ArrayList<Double> listZ2G = new ArrayList<>();
    ArrayList<Double> listEuclidNorm2G = new ArrayList<>();

    //gyroscope sensor
    ArrayList<Double> listXA = new ArrayList<>();
    ArrayList<Double> listYA = new ArrayList<>();
    ArrayList<Double> listZA = new ArrayList<>();
    ArrayList<Double> listEuclidNormA = new ArrayList<>();
    //list for second level filter
    ArrayList<Double> listX2A = new ArrayList<>();
    ArrayList<Double> listY2A = new ArrayList<>();
    ArrayList<Double> listZ2A = new ArrayList<>();
    ArrayList<Double> listEuclidNorm2A = new ArrayList<>();

    LineChart lineChart;
    ArrayList<Double> listXGraph = new ArrayList<>();

    ArrayList<Double> listcheckthresshold = new ArrayList<>();
    double variableXmiddle, //variableX, variableZ, variableEuclidNorm,
            variableY, variableYmiddle,  variableZmiddle = 0;
    //
    FileWriter writer, writer2, writer3, writer4;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    private int filenumber = 0;
    private int samemovementrising, samemovementfalling = 0;

    private boolean fiveSecond = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        lineChart = (LineChart) findViewById(R.id.lineChart);
        eta = (TextView)findViewById(R.id.eta);
        xView = (TextView)findViewById(R.id.xView);
      //  yView = (TextView)findViewById(R.id.yView);
      //  vView = (TextView)findViewById(R.id.vView);

        xVariance = (TextView)findViewById(R.id.xVariance);
        yVariance = (TextView)findViewById(R.id.yVariance);
        zVariance = (TextView)findViewById(R.id.zVariance);
        totalVariance = (TextView)findViewById(R.id.total);


        Button startBtn = (Button)findViewById(R.id.startBtn);
        Button saveBtn = (Button)findViewById(R.id.button2);
        Button analyzeBtn = (Button)findViewById(R.id.analyze);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senAccelerometerNormal = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

/*        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lastLocation = location;
                coordinates.setText("lat: " + location.getLatitude() + "  Lng: " +location.getLongitude());
            }
        }; */



        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        startBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //shared preference for files
                sharedpreferences = getSharedPreferences("your_prefs", Activity.MODE_PRIVATE);
                filenumber = sharedpreferences.getInt("filenumberkey", -1);
                filenumber++;
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("filenumberkey", filenumber);
                editor.commit();
                eta.setText("File number: " + filenumber);
               // hideSystemUI();
                startSensors();

                delayTimer();

            }

        });
     /*   saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideSystemUI();
            }
        });

        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  analyzeList();

            }
        });  */
    }
/*
    public void analyzeList() {

        thresholdSkiiX = 0.0;
        thresholdExist = true;

        for (Iterator<Double> i = EmilSkive.iterator(); i.hasNext();) {
            double item = i.next();
            listXThreshold.add(item);
            listXSample.add(item);

            if (listXThreshold != null) {
                //set sample size
                if (listXSample.size() > 11 && thresholdExist) {

                    if (sampleXNew.size() == 0 && listXSample.size() < 13) {
                        sampleXNew.addAll(listXSample);
                    } else if (sampleXNew.size() != 0) {
                        sampleXNew.clear();
                        sampleXNew.addAll(listXSample);
                    }

                    if (calculateAverage(sampleXNew) < thresholdSkiiX && thresholdSkiiX > calculateAverage(sampleXOld) && risingX
                             && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) > 0.2) {
                        longestTurnX = 0;
                        countRegulationX++;
                        xView.setText("x turns: " + countRegulationX);

                     //   Log.v("Turn cakey", " Turn: " + countRegulationX);
                       // Log.v("Turn cakey2", " sampleXNew: " + calculateAverage(sampleXNew) + " sampleXOld: " + calculateAverage(sampleXOld));
                    } else if (calculateAverage(sampleXNew) > thresholdSkiiX && thresholdSkiiX < calculateAverage(sampleXOld) && fallingX
                            && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) < 0.2) {
                        longestTurnX = 0;
                        countRegulationX++;
                        xView.setText("x turns: " + countRegulationX);
                        Log.w("thresshold", "passed: " + thresholdSkiiX);
                    }

                    //count all tops
                    //falling
                    if (calculateAverage(sampleXNew) < calculateAverage(sampleXOld) && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) > 0.2) {

                        if (samemovementfalling >= 1) {
                            topsX++;
                            Log.w("upbeat", "topsX1: " + topsX);
                        }
                        fallingX = true;
                        risingX = false;
                        samemovementrising++;
                        samemovementfalling = 0;
                    }
                    if (calculateAverage(sampleXNew) > calculateAverage(sampleXOld) &&   Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) < 0.2) {
                        if (samemovementrising >= 1) {
                            topsX++;
                            Log.w("upbeat", "topsX2: " + topsX);
                        }
                        samemovementfalling++;
                        samemovementrising = 0;
                        fallingX = false;
                        risingX = true;
                    }


                    //set new register to old for x
                    sampleXOld.clear();
                    sampleXOld.addAll(sampleXNew);
                    //reset
                    listXSample.clear();
                }
                //big sample size
                if (listXThreshold.size() > 500) {
                    thresholdExist = true;
                    Log.v("Together", " variableXmiddle: " + variableXmiddle + " stdDev: " + stdDev);

                    statsEuclidX = new Statistics(listXThreshold);
                    stdDevX = statsEuclidX.getStdDev();
                    minX = statsEuclidX.getMin();
                    maxX = statsEuclidX.getMax();
                    thresholdSkiiX = (minX + maxX) / 2;

                    // empty the list
                    listXThreshold.clear();
                }
            }
        }
    } */


    public void turnTimer(){
        //interval for performing a check
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

               // Log.d("timestuff",""+longestTurn);
                shortestTurnbool = true;
                h.postDelayed(this, shortestTurn);

                longestTurn = longestTurn + shortestTurn;
                longestTurnX = longestTurnX + shortestTurn;
                longestTurnY = longestTurnY + shortestTurn;
                if(longestTurn > 5999){
                    //longestTurn = 0;
                    LongestTurnboolMissed = true;
                }

            }
        }, 1000); // set the measure to milliseconds
    }
    public void delayTimer(){

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fiveSecond = true;
                // Do something after 5s = 5000ms
                Log.v("testtesttest", "5 sec");

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(3000);

                delayTimerStart();
            }
            //10seconds
        }, 15000);
    }

    public void delayTimerStart(){

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fiveSecond = true;
                // Do something after 5s = 5000ms

                recordtime();
            }
            //10seconds
        }, 5000);
    }

    public void recordtime(){

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                fiveSecond = false;

                try {
                    writer.close();
                    writer2.close();
                   // writer3.close();
                  //  writer4.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(4000);

              //  ArrayList<String> xAXES = new ArrayList<>();
                ArrayList<Entry> yAXESsin = new ArrayList<>();
                ArrayList<Entry> yAXEScos = new ArrayList<>();

                for(int i=0; i < 1000;i++){
                    double d = listXGraph.get(i);
                    float grapplot = (float)d;

                    yAXESsin.add(new Entry(grapplot,i));

                }
             /*   for(int i=0;i<numDataPoints;i++){
                    float sinFunction = Float.parseFloat(String.valueOf(Math.sin(x)));
                    float cosFunction = Float.parseFloat(String.valueOf(Math.cos(x)));
                    x = x + 0.1;
                    yAXESsin.add(new Entry(sinFunction,i));
                    yAXEScos.add(new Entry(cosFunction,i));
                    xAXES.add(i, String.valueOf(x));
                } */
          /*       String[] xaxes = new String[xAXES.size()];
                for(int i=0; i<xAXES.size();i++){
                    xaxes[i] = xAXES.get(i).toString();
                } */


                LineDataSet lineDataSet1 = new LineDataSet(yAXESsin,"x aksel");


                lineDataSet1.setDrawCircles(false);
                lineDataSet1.setColor(Color.BLUE);


                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setTextColor(Color.RED);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(true);

                lineChart.setData(new LineData(lineDataSet1));




              //  lineChart.setVisibleXRangeMaximum(250f);
                listXGraph.clear();

            }
            //35seconds
        }, 32000);
    }

    private void hideSystemUI() {
        int uiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;

        }
/*
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }  */
        Log.v("fullscreen", "" + newUiOptions);
        this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    //google maps stuff

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
        }
    }
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionCheck >= 0) {
            // The final argument to {@code requestLocationUpdates()} is a LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        else {

            // functionality that depends on this permission.
            Toast.makeText(this, "Permission denied to access your location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();


        super.onStop();
    }
    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
       // Log.i(this, "Connected to GoogleApiClient");

        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck >= 0) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        //updateUI();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("d", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("d", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void startSensors() {
        //game is 50Hz
        try {
            File path = this.getExternalFilesDir(null);
            writer = new FileWriter(new File(path,"XYZV"+ filenumber + ".csv"));
            writer2 = new FileWriter(new File(path,"STDTurn"+ filenumber + ".csv"));

          //  writer3 = new FileWriter(new File(path,"GyroScope"+ filenumber + ".csv"));

           // writer4 = new FileWriter(new File(path,"NormalXYZV"+ filenumber + ".csv"));

            writer.write("time,x,y,z,v" +"\n");

            writer.flush();


                    //writer = new FileWriter("/mnt/sdcard/"+"output.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_GAME);
        senSensorManager.registerListener(this, senAccelerometerNormal, SensorManager.SENSOR_DELAY_GAME);
        turnTimer();
/*
        //TODO:add speed into judging of level, until then don't use
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } */
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
    if(fiveSecond) {


        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //digital filter
            double dX = (double) sensorEvent.values[0];
            double dY = (double) sensorEvent.values[1];
            double dZ = (double) sensorEvent.values[2];
            listX.add(dX);
            listY.add(dY);
            listZ.add(dZ);
            //DONE: change over to force a.k.a euclidNorm
            listEuclidNorm.add(Math.sqrt(dX * dX + dY * dY + dZ * dZ));
        }
  /*      if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //digital filter

            double dXG = (double) sensorEvent.values[0];
            double dYG = (double) sensorEvent.values[1];
            double dZG = (double) sensorEvent.values[2];
            listXG.add(dXG);
            listYG.add(dYG);
            listZG.add(dZG);
            //DONE: change over to force a.k.a euclidNorm
            listEuclidNormG.add(Math.sqrt(dXG * dXG + dYG * dYG + dZG * dZG));
        }

        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //digital filter

            double dXA = (double) sensorEvent.values[0];
            double dYA = (double) sensorEvent.values[1];
            double dZA = (double) sensorEvent.values[2];
            listXA.add(dXA);
            listYA.add(dYA);
            listZA.add(dZA);
            //DONE: change over to force a.k.a euclidNorm
            listEuclidNormA.add(Math.sqrt(dXA * dXA + dYA * dYA + dZA * dZA));
        }

        //set the amount to average over
         if (listEuclidNormA.size() >= 10) {
            double variableXA = calculateAverage(listXA);
            listXA.remove(0);
            listX2A.add(variableXA);

            double variableYA = calculateAverage(listYA);
            listYA.remove(0);
            listY2A.add(variableYA);

            double variableZA = calculateAverage(listZA);
            listZA.remove(0);
            listZ2A.add(variableZA);

            double variableEuclidNormA = calculateAverage(listEuclidNormA);
            listEuclidNormA.remove(0);
            listEuclidNorm2A.add(variableEuclidNormA);
            //Log.v("Together", " variableEuclidNorm: "+ variableEuclidNorm);
        }
        // second level
        if (listEuclidNorm2A.size() >= 10) {
            double variableXA = calculateAverage(listX2A);
            listX2A.remove(0);

            double variableYA = calculateAverage(listY2A);
            listY2A.remove(0);

            double variableZA = calculateAverage(listZ2A);
            listZ2A.remove(0);

            double variableEuclidNormA = calculateAverage(listEuclidNorm2A);
            listEuclidNorm2A.remove(0);

            //write to file
            try {
                //Log.d("test of time", ": " + (System.currentTimeMillis()- initialTime));
                writer4.write((System.currentTimeMillis() - initialTime) + "," + Double.toString(variableXA) + "," + Double.toString(variableYA) + "," +
                        Double.toString(variableZA) + "," + Double.toString(variableEuclidNormA) + "\n");
                writer4.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //set the amount to average over
        if (listEuclidNormG.size() >= 10) {
            double variableXG = calculateAverage(listXG);
            listXG.remove(0);
            listX2G.add(variableXG);

            double variableYG = calculateAverage(listYG);
            listYG.remove(0);
            listY2G.add(variableYG);

            double variableZG = calculateAverage(listZG);
            listZG.remove(0);
            listZ2G.add(variableZG);

            double variableEuclidNormG = calculateAverage(listEuclidNormG);
            listEuclidNormG.remove(0);
            listEuclidNorm2G.add(variableEuclidNormG);
            //Log.v("Together", " variableEuclidNorm: "+ variableEuclidNorm);
        }
        // second level
        if (listEuclidNorm2G.size() >= 10) {
            double variableXG = calculateAverage(listX2G);
            listX2G.remove(0);

            double variableYG = calculateAverage(listY2G);
            listY2G.remove(0);

            double variableZG = calculateAverage(listZ2G);
            listZ2G.remove(0);

            double variableEuclidNormG = calculateAverage(listEuclidNorm2G);
            listEuclidNorm2G.remove(0);

            //write to file
            try {
                //Log.d("test of time", ": " + (System.currentTimeMillis()- initialTime));
                writer3.write((System.currentTimeMillis() - initialTime) + "," + Double.toString(variableXG) + "," + Double.toString(variableYG) + "," +
                        Double.toString(variableZG) + "," + Double.toString(variableEuclidNormG) + "\n");
                writer3.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
*/
        //set the amount to average over
        if (listEuclidNorm.size() >= 10) {
            double variableX = calculateAverage(listX);
            listX.remove(0);
            listX2.add(variableX);

            double variableY = calculateAverage(listY);
            listY.remove(0);
            listY2.add(variableY);

            double variableZ = calculateAverage(listZ);
            listZ.remove(0);
            listZ2.add(variableZ);

            double variableEuclidNorm = calculateAverage(listEuclidNorm);
            listEuclidNorm.remove(0);
            listEuclidNorm2.add(variableEuclidNorm);
            //Log.v("Together", " variableEuclidNorm: "+ variableEuclidNorm);
        }
        // second level
        if (listEuclidNorm2.size() >= 10) {
            double variableX = calculateAverage(listX2);
            listX2.remove(0);
            listXThreshold.add(variableX);
            listXSample.add(variableX);

            listXGraph.add(variableX);


            double variableY = calculateAverage(listY2);
            listY2.remove(0);
            listYThreshold.add(variableY);
            listYSample.add(variableY);

            double variableZ = calculateAverage(listZ2);
            listZ2.remove(0);
            listZThreshold.add(variableZ);
            listZSample.add(variableZ);

            double variableEuclidNorm = calculateAverage(listEuclidNorm2);
            listEuclidNorm2.remove(0);
            listEuclidNormThreshold.add(variableEuclidNorm);
            listEuclidNormSample.add(variableEuclidNorm);
            //Log.v("Together", " variableEuclidNorm: "+ variableEuclidNorm);

            //write to file
            try {
                //Log.d("test of time", ": " + (System.currentTimeMillis()- initialTime));
                writer.write((System.currentTimeMillis()- initialTime) + "," + Double.toString(variableX) + "," + Double.toString(variableY) + "," +
                        Double.toString(variableZ) + "," + Double.toString(variableEuclidNorm) + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //de tre
        //listXSample ? sampleXNew-> sampleXOld
        if (listEuclidNormThreshold != null) {
            //set sample size
            if (listEuclidNormSample.size() > 12 && thresholdExist) {

            /*    double sumStart = listEuclidNormSample.get(0);
                double sumEnd = listEuclidNormSample.get(11);
                double sum = Math.abs(sumStart)-Math.abs(sumEnd);
                //Log.v("noise", "difference between start and end: " +sum);
                */
                //first sample get set

                //Log.v("fuuuuuuuuuuuuuu", " sampleXNew: " + sampleXNew.size() + " listXSample: " + listXSample.size());
                if (SampleEuclidNormNew.size() == 0 && listEuclidNormSample.size() < 13) {
                    SampleEuclidNormNew.addAll(listEuclidNormSample);
                }
                //TODO:set a precision for the if
                else if (SampleEuclidNormNew.size() != 0) {
                    SampleEuclidNormNew.clear();
                    SampleEuclidNormNew.addAll(listEuclidNormSample);
                }

                if (sampleXNew.size() == 0 && listXSample.size() < 13) {
                    sampleXNew.addAll(listXSample);
                } else if (sampleXNew.size() != 0) {
                    sampleXNew.clear();
                    sampleXNew.addAll(listXSample);
                }

                if (sampleYNew.size() == 0 && listYSample.size() < 13) {
                    sampleYNew.addAll(listYSample);
                } else if (sampleYNew.size() != 0) {
                    sampleYNew.clear();
                    sampleYNew.addAll(listYSample);
                }

                //  Log.v("fuuuuuuuuuuuuuu", " SampleEuclidNormNew: " + calculateAverage(SampleEuclidNormNew)  + " sampleEuclidNormOld: " + calculateAverage(sampleEuclidNormOld));
                // else{ countRegulation = 0; }

                //Log.v("pre-Cakey", " lowest number: " + new Statistics(sampleXNew).getMin() + " threshold: " +thresholdSkiiX + " countRegulation: " + countRegulation);
                //decision area, TODO: Set up the time interval
                if (calculateAverage(SampleEuclidNormNew) < thresholdSkii && thresholdSkii > calculateAverage(sampleEuclidNormOld)
                        && longestTurn > 500 && Math.abs(calculateAverage(SampleEuclidNormNew)) - Math.abs(calculateAverage(sampleEuclidNormOld)) > 0.2) {
                    longestTurn = 0;
                    countRegulationEuclid++;
                //    vView.setText("|v| turns: " + countRegulationEuclid);
                    //    Log.v("Turn cakey", " Turn: "+ countRegulationEuclid );
                    //    Log.v("Turn cakey2", " SampleEuclidNormNew: "+ calculateAverage(SampleEuclidNormNew) + " sampleEuclidNormOld: "+ calculateAverage(sampleEuclidNormOld));

                }
                if (calculateAverage(sampleXNew) < thresholdSkiiX && thresholdSkiiX > calculateAverage(sampleXOld) && risingX
                        && longestTurnX > 500 && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) > 0.2) {
                    longestTurnX = 0;
                    countRegulationX++;
                    xView.setText("x turns: " + countRegulationX);
                    Log.v("Turn cakey", " Turn: " + countRegulationX);
                    Log.v("Turn cakey2", " sampleXNew: " + calculateAverage(sampleXNew) + " sampleXOld: " + calculateAverage(sampleXOld));
                } else if (calculateAverage(sampleXNew) > thresholdSkiiX && thresholdSkiiX < calculateAverage(sampleXOld) && fallingX
                        && longestTurnX > 500 && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) < 0.2) {
                    longestTurnX = 0;
                    countRegulationX++;
                    xView.setText("x turns: " + countRegulationX);
                }
                if (calculateAverage(sampleYNew) < thresholdSkiiY && thresholdSkiiY > calculateAverage(sampleYOld)
                        && longestTurnY > 500 && Math.abs(calculateAverage(sampleYNew)) - Math.abs(calculateAverage(sampleYOld)) > 0.2) {
                    longestTurnY = 0;
                    countRegulationY++;
              //      yView.setText("y turns: " + countRegulationY);
                } else if (calculateAverage(sampleYNew) > thresholdSkiiY && thresholdSkiiY < calculateAverage(sampleYOld)
                        && longestTurnY > 500 && Math.abs(calculateAverage(sampleYNew)) - Math.abs(calculateAverage(sampleYOld)) < 0.2) {
                    longestTurnY = 0;
                    countRegulationY++;
              //      yView.setText("y turns: " + countRegulationY);
                }

                //count all tops
                //falling
                if (calculateAverage(sampleXNew) < calculateAverage(sampleXOld) && Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) > 0.2) {
                    fallingX = true;
                    risingX = false;
                    if (samemovementfalling >= 1) {
                        topsX++;
                        Log.w("upbeat", "topsX: " + topsX);
                    }
                    samemovementrising++;
                    samemovementfalling = 0;
                }
                if (calculateAverage(sampleXNew) > calculateAverage(sampleXOld) &&
                        Math.abs(calculateAverage(sampleXNew)) - Math.abs(calculateAverage(sampleXOld)) < 0.2) {
                    if (samemovementrising >= 1) {
                        topsX++;
                        Log.w("upbeat", "topsX: " + topsX);
                    }
                    samemovementfalling++;
                    samemovementrising = 0;
                    fallingX = false;
                    risingX = true;
                }

                //set new register to old
                sampleEuclidNormOld.clear();
                sampleEuclidNormOld.addAll(SampleEuclidNormNew);
                //set new register to old for x
                sampleXOld.clear();
                sampleXOld.addAll(sampleXNew);
                //set new register to old for y
                sampleYOld.clear();
                sampleYOld.addAll(sampleYNew);
                //reset
                listXSample.clear();
                listYSample.clear();
                listZSample.clear();
                listEuclidNormSample.clear();
            }
            //big sample size
            if (listEuclidNormThreshold.size() > 300) {
                thresholdExist = true;

                //
                statsEuclid = new Statistics(listEuclidNormThreshold);
                stdDev = statsEuclid.getStdDev();
                min = statsEuclid.getMin();
                max = statsEuclid.getMax();
                thresholdSkii = (min + max) / 2;

                variableXmiddle = calculateAverage(listEuclidNormThreshold);
                Log.v("Together", " variableXmiddle: " + variableXmiddle + " stdDev: " + stdDev);
                //System.out.println("Min value " + min + " Max value " + max + " thresholdSkii" + thresholdSkii);

                // x stdDev is variance instead
                statsEuclidX = new Statistics(listXThreshold);
                stdDevX = statsEuclidX.getVariance();
                minX = statsEuclidX.getMin();
                maxX = statsEuclidX.getMax();
                thresholdSkiiX = (minX + maxX) / 2;

                // y stdDev is variance instead
                statsEuclidY = new Statistics(listYThreshold);
                stdDevY = statsEuclidY.getVariance();
                minY = statsEuclidY.getMin();
                maxY = statsEuclidY.getMax();

                thresholdSkiiY = (minY + maxY) / 2;

                // stdDev is variance instead
                statsEuclidZ = new Statistics(listZThreshold);
                stdDevZ = statsEuclidZ.getVariance();
                minZ = statsEuclidZ.getMin();
                maxZ = statsEuclidZ.getMax();
                thresholdSkiiZ = (minZ + maxZ) / 2;

                if(stdDevY > yDifference){
                    yDifference = stdDevY;
                }
                if(stdDevX > xDifference){
                    xDifference = stdDevX;
                }
                if(stdDevZ > zDifference){
                    zDifference = stdDevZ;
                }

                double total = yDifference + xDifference + zDifference;


                double amountTurns;
                if (yDifference > xDifference) {
                    stdDevs = yDifference;
                    amountTurns = countRegulationY;

                } else {
                    stdDevs = xDifference;
                    amountTurns = countRegulationX;
                }

                //set the amount of decimals
                Double truncatedDoubleX = BigDecimal.valueOf(xDifference)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                Double truncatedDoubleY = BigDecimal.valueOf(yDifference)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                Double truncatedDoubleZ = BigDecimal.valueOf(zDifference)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                Double truncatedDoubleTotal = BigDecimal.valueOf(total)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                xVariance.setText("" + truncatedDoubleX);
                yVariance.setText("" + truncatedDoubleY);
                zVariance.setText(""+ truncatedDoubleZ);
                totalVariance.setText("" + truncatedDoubleTotal);

                //clock, deviant,amount of turns,
                try {
                    writer2.write((System.currentTimeMillis()- initialTime) + "," + Double.toString(stdDevs) +
                            "," + amountTurns + "," // + mCurrentLocation.getSpeed()
                            + "\n");
                    writer2.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // empty the list
                listXThreshold.clear();
                listYThreshold.clear();
                listZThreshold.clear();
                listEuclidNormThreshold.clear();
            }
        }
    } else{
        initialTime = System.currentTimeMillis();
    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    private double calculateAverage(List <Double> marks) {
        double sum = 0;
    //    Log.v("this is", "size:" + marks.size());
        if(!marks.isEmpty()) {
            for (double mark : marks) {
                sum += mark;
    //
            }
            return sum / marks.size();
        }
        return sum;
    }
}
