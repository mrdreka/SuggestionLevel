package com.example.speciale.suggestion;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


import weka.core.Instance;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements SensorEventListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

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


    TextView xView, yView, vView, eta , accSamples,infered ;
    ImageView transportIMG;
   // List<AccObj> accObjList = new ArrayList<AccObj>();
    Statistics statsEuclid,statsEuclidY, statsEuclidX ;

    private double mean, stdDev, min, max, thresholdSkii, stdDevX, minX,maxX, thresholdSkiiX;
    private double minY, maxY, stdDevY, thresholdSkiiY;
    //private double minZ, maxZ, thresholdSkiiZ;


    private int shortestTurn = 100;
    //longest is calcualted with amount of shortturn, so
    private int longestTurn, longestTurnX, longestTurnY = 0;
    private int countRegulationEuclid, countRegulationX,  countRegulationY= 0;


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

    double variableXmiddle, //variableX, variableZ, variableEuclidNorm,
            variableY, variableYmiddle,  variableZmiddle = 0;
    //
    FileWriter writer, writer2;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    private int filenumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
        eta = (TextView)findViewById(R.id.eta);
        xView = (TextView)findViewById(R.id.xView);
        yView = (TextView)findViewById(R.id.yView);
        vView = (TextView)findViewById(R.id.vView);

        transportIMG = (ImageView)findViewById(R.id.transportIMG);


        Button startBtn = (Button)findViewById(R.id.startBtn);
        Button saveBtn = (Button)findViewById(R.id.button2);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

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

                startSensors();
            }

        });

    }

    public void turnTimer(){
        //interval for performing a check
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                Log.d("timestuff",""+longestTurn);
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
/*
    public void setETA(int type) {

        final Location storcenterNord = new Location("");
        storcenterNord.setLatitude(56.1704202d);
        storcenterNord.setLongitude(10.188441399999988d);

        double bikeSpeed = 5.5d; //M/S
        double walkSpeed = 1.39d;
        double driveSpeed = 13.5d;

        if (mCurrentLocation != null) {
            double distance = mCurrentLocation.distanceTo(storcenterNord);

            if (type == 0) {
                eta.setText( (int) (distance / walkSpeed)/60 +" min");
            }

            if (type == 1) {
                eta.setText( (int) (distance / bikeSpeed)/60 +" min");
            }
            if (type == 2) {
                eta.setText( (int) (distance / driveSpeed)/60 +" min");
            }
        }
        else{
            Log.v("d" ,"my location: " + mCurrentLocation);

        }
    } */

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

    /*    try {
            writer.close();
            writer2.close();
        } catch (IOException e) {
            e.printStackTrace();
        } */


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

            //writer = new FileWriter("/mnt/sdcard/"+"output.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        turnTimer();

        //TODO:add speed into judging of level, until then don't use
        /*if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } */
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Calendar c = Calendar.getInstance();

        //digital filter
        double dX = (double)sensorEvent.values[0];
        double dY = (double)sensorEvent.values[1];
        double dZ = (double)sensorEvent.values[2];

        listX.add(dX);
        listY.add(dY);
        listZ.add(dZ);
        //TODO: change over to force a.k.a euclidNorm
        listEuclidNorm.add(Math.sqrt(dX*dX+dY*dY+dZ*dZ));

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
                writer.write(c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+ "."+c.get(Calendar.MILLISECOND) + ","  + Double.toString(variableX) + "," + Double.toString(variableY) + "," +
                        Double.toString(variableZ) + "," + Double.toString(variableEuclidNorm) + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //de tre
        //listXSample ? sampleXNew-> sampleXOld
        if (listEuclidNormThreshold != null) {
            //set sample size
            if (listEuclidNormSample.size()  > 11 && thresholdExist) {

            /*    double sumStart = listEuclidNormSample.get(0);
                double sumEnd = listEuclidNormSample.get(11);
                double sum = Math.abs(sumStart)-Math.abs(sumEnd);
                //Log.v("noise", "difference between start and end: " +sum);
                */
                //first sample get set

                //Log.v("fuuuuuuuuuuuuuu", " sampleXNew: " + sampleXNew.size() + " listXSample: " + listXSample.size());
                if (SampleEuclidNormNew.size() == 0 && listEuclidNormSample.size() < 13 ) {
                    SampleEuclidNormNew.addAll(listEuclidNormSample);
                }
                //TODO:set a precision for the if
                else if(SampleEuclidNormNew.size() != 0)
                {
                    SampleEuclidNormNew.clear();
                    SampleEuclidNormNew.addAll(listEuclidNormSample);
                }

                if (sampleXNew.size() == 0 && listXSample.size() < 13 ) {
                    sampleXNew.addAll(listXSample);
                }
                else if(sampleXNew.size() != 0)
                {
                    sampleXNew.clear();
                    sampleXNew.addAll(listXSample);
                }

                if (sampleYNew.size() == 0 && listYSample.size() < 13 ) {
                    sampleYNew.addAll(listYSample);
                }
                else if(sampleYNew.size() != 0)
                {
                    sampleYNew.clear();
                    sampleYNew.addAll(listYSample);
                }

              //  Log.v("fuuuuuuuuuuuuuu", " SampleEuclidNormNew: " + calculateAverage(SampleEuclidNormNew)  + " sampleEuclidNormOld: " + calculateAverage(sampleEuclidNormOld));
               // else{ countRegulation = 0; }

                //Log.v("pre-Cakey", " lowest number: " + new Statistics(sampleXNew).getMin() + " threshold: " +thresholdSkiiX + " countRegulation: " + countRegulation);
                //decision area, TODO: Set up the time interval
                if(calculateAverage(SampleEuclidNormNew) < thresholdSkii && thresholdSkii > calculateAverage(sampleEuclidNormOld)
                        && longestTurn >500 && Math.abs(calculateAverage(SampleEuclidNormNew))-Math.abs(calculateAverage(sampleEuclidNormOld)) > 0.2 ){
                    longestTurn = 0;
                    countRegulationEuclid++;
                    vView.setText("|v| turns: " + countRegulationEuclid);
                //    Log.v("Turn cakey", " Turn: "+ countRegulationEuclid );
             //       Log.v("Turn cakey2", " SampleEuclidNormNew: "+ calculateAverage(SampleEuclidNormNew) + " sampleEuclidNormOld: "+ calculateAverage(sampleEuclidNormOld));

                }
                if(calculateAverage(sampleXNew) < thresholdSkiiX && thresholdSkiiX > calculateAverage(sampleXOld)
                        && longestTurnX >500 && Math.abs(calculateAverage(sampleXNew))-Math.abs(calculateAverage(sampleXOld)) > 0.2 ){
                    longestTurnX = 0;
                    countRegulationX++;
                    xView.setText("x turns: " + countRegulationX);
                    Log.v("Turn cakey", " Turn: "+ countRegulationX );
                    Log.v("Turn cakey2", " sampleXNew: "+ calculateAverage(sampleXNew) + " sampleXOld: "+ calculateAverage(sampleXOld));
                } else if(calculateAverage(sampleXNew) > thresholdSkiiX && thresholdSkiiX < calculateAverage(sampleXOld)
                        && longestTurnX >500 && Math.abs(calculateAverage(sampleXNew))-Math.abs(calculateAverage(sampleXOld)) < 0.2 ){
                    longestTurnX = 0;
                    countRegulationX++;
                    xView.setText("x turns: " + countRegulationX);
                }
                if(calculateAverage(sampleYNew) < thresholdSkiiY && thresholdSkiiY > calculateAverage(sampleYOld)
                        && longestTurnY >500 && Math.abs(calculateAverage(sampleYNew))-Math.abs(calculateAverage(sampleYOld)) > 0.2 ){
                    longestTurnY = 0;
                    countRegulationY++;
                    yView.setText("y turns: " + countRegulationY);
                }
                else if(calculateAverage(sampleYNew) > thresholdSkiiY && thresholdSkiiY < calculateAverage(sampleYOld)
                        && longestTurnY >500 && Math.abs(calculateAverage(sampleYNew))-Math.abs(calculateAverage(sampleYOld)) < 0.2 ){
                    longestTurnY = 0;
                    countRegulationY++;
                    yView.setText("y turns: " + countRegulationY);
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
            if (listEuclidNormThreshold.size() > 250) {
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

                //
                statsEuclidX = new Statistics(listXThreshold);
                stdDevX = statsEuclidX.getStdDev();
                minX = statsEuclidX.getMin();
                maxX = statsEuclidX.getMax();
                thresholdSkiiX = (minX + maxX) / 2;

                //
                statsEuclidY = new Statistics(listYThreshold);
                stdDevY = statsEuclidY.getStdDev();
                minY = statsEuclidY.getMin();
                maxY = statsEuclidY.getMax();
                thresholdSkiiY = (minY + maxY) / 2;

                double yDifference = maxY-(minY);
                double xDifference = maxX-(minX);
                double stdDevs;


                if(yDifference > xDifference){
                    stdDevs = yDifference;

                }
                else{
                    stdDevs = xDifference;
                }


                //write to file
                try {
                    writer2.write(c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+ "."+c.get(Calendar.MILLISECOND) + "," + Double.toString(stdDevs) + "," + "speed" + "\n");
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
