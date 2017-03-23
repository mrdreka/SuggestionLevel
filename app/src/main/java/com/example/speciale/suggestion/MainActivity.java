package com.example.speciale.suggestion;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.location.Location;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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


    TextView xVal,yVal,zVal, infered, accSamples, eta, coordinates;
    ImageView transportIMG;
    List<AccObj> accObjList = new ArrayList<AccObj>();
    Statistics statsEuclid;
    private Instance iUse;

    private double mean, stdDev, min, max;
    private boolean bike, walk, drive = false;
    private double numBiking, numWalking = 0;
    private int type;
    Location lastLocation;

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
        coordinates = (TextView)findViewById(R.id.coordinates);


        transportIMG = (ImageView)findViewById(R.id.transportIMG);


        Button startBtn = (Button)findViewById(R.id.startBtn);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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


        startBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                startSensors();
            }
        });

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
    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
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


    }

    public void startSensors() {
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
        /*
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck >= 0) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        }
        else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Toast.makeText(this, "Permission denied to access your location", Toast.LENGTH_SHORT).show();
        } */

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

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
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


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // xVal.setText("X: " +(int)sensorEvent.values[0]);
        // yVal.setText("Y: " +(int)sensorEvent.values[1]);
        // zVal.setText("Z: " +(int)sensorEvent.values[2]);

        AccObj bla = new AccObj(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        List<Double> euclidNormArray = new ArrayList();

        if (accObjList.size() < 129) {

            accObjList.add(bla);
            //    accSamples.setText(accObjList.size() + " samples");


        }

        if (accObjList.size() == 128) {

            for (int i = 0; i < accObjList.size(); i++) {

                euclidNormArray.add(
                        Math.sqrt( Math.pow(accObjList.get(i).getX(),2) + Math.pow(accObjList.get(i).getY(),2) + Math.pow(accObjList.get(i).getZ(),2) )
                );
            }

            statsEuclid = new Statistics((ArrayList<Double>) euclidNormArray);

            mean = statsEuclid.getMean() ;
            stdDev = statsEuclid.getStdDev();
            min = statsEuclid.getMin();
            max = statsEuclid.getMax();

            Log.v("MainActivity", "Mean: "+ mean + " - stdDev: " + stdDev + " - min: " + min + " -  max: " +max );


            Attribute attributeMean = new Attribute("mean");
            Attribute attributeStdDev = new Attribute("stdDeviation");
            Attribute attributeMin = new Attribute("min");
            Attribute attributeMax = new Attribute("max");

            // Declare the class attribute along with its values
            FastVector fvClassVal = new FastVector(5);
            fvClassVal.addElement("walking");
            fvClassVal.addElement("biking");
            fvClassVal.addElement("driving");
            Attribute ClassAttribute = new Attribute("movementType", fvClassVal);

            // Declare the feature vector
            FastVector fvWekaAttributes = new FastVector(5);
            fvWekaAttributes.addElement(attributeMean);
            fvWekaAttributes.addElement(attributeStdDev);
            fvWekaAttributes.addElement(attributeMin);
            fvWekaAttributes.addElement(attributeMax);

            fvWekaAttributes.addElement(ClassAttribute);

            // Create an empty training set
            Instances isTrainingSet = new Instances("Rel", fvWekaAttributes, 10);
            // Set class index
            isTrainingSet.setClassIndex(4);

            //Our instance
            iUse = new Instance(isTrainingSet.numAttributes());
            isTrainingSet.add(iUse);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(0), mean);
            iUse.setValue((Attribute)fvWekaAttributes.elementAt(1), stdDev);
            iUse.setValue((Attribute) fvWekaAttributes.elementAt(2), min);
            iUse.setValue((Attribute) fvWekaAttributes.elementAt(3), max);
            iUse.setMissing(4);
            iUse.setDataset(isTrainingSet);

            Classifier cls = null;
            try {

                cls = (Classifier) SerializationHelper.read("/mnt/sdcard/classifier.model");
                double fDistribution = cls.classifyInstance(iUse);

                //        Log.v("MainActivity", "Model Read : success");

                if((int) fDistribution == 0){

                    transportIMG.setImageResource(R.drawable.walk);


                    Log.v("MainActivity", "Walking");
                    numWalking ++;
                    //  infered.setText("WALKING - walking: " + numWalking + " - biking: " + numBiking);

                    walk = true;
                    bike = false;
                    drive = false;

                    mean = 0;
                    stdDev = 0;
                    min = 0;
                    max =0;

                    setETA(0);
                }

                if((int) fDistribution == 1){

                    transportIMG.setImageResource(R.drawable.bike);

                    Log.v("MainActivity", "Biking");
                    numBiking ++;
                    //    infered.setText("BIKING - walking: " + (int)numWalking + " - biking: " + (int)numBiking);



                    walk = false;
                    bike = bike;
                    drive = false;

                    mean = 0;
                    stdDev = 0;
                    min = 0;
                    max =0;

                    setETA(1);

                }

                if((int) fDistribution == 2){

                    transportIMG.setImageResource(R.drawable.car);


                    Log.v("MainActivity", "Driving");

                    //  infered.setText("Driving");

                    walk = false;
                    bike = false;
                    drive = true;

                    mean = 0;
                    stdDev = 0;
                    min = 0;
                    max =0;

                    setETA(2);

                }


            } catch (Exception e) {
                e.printStackTrace();
            }



            for (int i = 0; i < 64; i++) {
                accObjList.remove(0);

            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private double calculateAverage(List <Integer> marks) {
        Integer sum = 0;
        if(!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
}
