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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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


    TextView xView, yView, vView, eta , accSamples,infered ;
    ImageView transportIMG;
   // List<AccObj> accObjList = new ArrayList<AccObj>();
    Statistics statsEuclid,statsEuclidY, statsEuclidX ;

    private double mean, stdDev, min, max, thresholdSkii, stdDevX, minX,maxX, thresholdSkiiX;
    private double minY, maxY, stdDevY, thresholdSkiiY, stdDevs;
    //private double minZ, maxZ, thresholdSkiiZ;


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


    //List<Double> EmilSkive = new ArrayList<>(Arrays.asList(-1.675929024,-1.570844576,-1.488340774,-1.442535636,-1.408834019,-1.439781454,-1.412783351,-1.347319241,-1.295496392,-1.235426636,-1.210254409,-1.116054468,-0.883830259,-0.586926405,-0.391901867,-0.245927038,-0.29184253,-0.407135422,-0.415828713,-0.410964902,-0.429688337,-0.527745829,-0.764120529,-1.027966891,-1.059543041,-0.8664908,-0.462446008,-0.010929861,0.243063921,0.378490353,0.50876612,0.661760949,0.873808452,1.092557248,1.17423271,1.089528109,0.911347228,0.72140645,0.63856249,0.643384973,0.644825376,0.633919613,0.610846725,0.598953421,0.618473089,0.683172909,0.75741172,0.844973925,0.940477026,1.047065386,1.191856271,1.334611478,1.4648127,1.560350738,1.630960132,1.6808702,1.732970766,1.752807578,1.757702762,1.734383504,1.686446472,1.634432408,1.59220916,1.557674552,1.508912011,1.474010191,1.477344635,1.537749975,1.639146763,1.7728263,1.896697184,2.008063868,2.069316194,2.099658068,2.162451603,2.196413847,2.151229063,2.051946217,1.913943413,1.815218598,1.806291587,1.868489647,2.025054336,2.239904106,2.381635063,2.50377985,2.674785624,2.817597449,2.857881579,2.741298537,2.57636955,2.388148503,2.265668867,2.169413155,2.186134824,2.275553706,2.287955912,2.327887304,2.538419145,2.893761998,3.145474738,3.357419802,3.386568807,3.361257309,3.304922905,3.186911219,3.181160988,3.230844523,3.237557244,3.188615396,3.25856976,3.319338861,3.397638879,3.440172862,3.410357651,3.422961066,3.405644833,3.390551035,3.371132194,3.338384352,3.237559794,3.152341745,3.092491145,3.08051441,3.156403682,3.24746686,3.336689942,3.390918419,3.404988499,3.423460865,3.449659777,3.442171547,3.452023458,3.415948266,3.322475802,3.159011269,2.957382547,2.754447381,2.511192936,2.390730205,2.357904764,2.40804641,2.541542275,2.740190548,2.928829823,3.120218039,3.321778078,3.517229185,3.820834792,3.875535892,3.806204162,3.70990275,3.488250475,3.236615545,3.058298604,2.886718436,2.702958143,2.505496105,2.278679078,2.216152443,2.158171822,2.045471164,1.990248123,1.943257722,1.827285666,1.772611728,1.71721754,1.609650734,1.438707094,1.212415277,1.023378916,0.867574207,0.654315993,0.381257553,0.058448102,-0.308369161,-0.614747985,-0.772116475,-0.81923591,-0.830682666,-0.829456123,-0.90628076,-1.013771079,-1.127453876,-1.192560598,-1.321339097,-1.562992193,-1.904195764,-2.245559446,-2.519737339,-2.813447107,-3.079086812,-3.298904016,-3.535514648,-3.759489534,-3.83221168,-3.618058414,-3.288117833,-3.172279072,-3.349339557,-3.707019191,-4.008663354,-4.26132237,-4.30163445,-4.115778823,-3.86661962,-4.092839041,-4.747270055,-5.3353232,-5.672955947,-5.61987854,-5.450235262,-5.210576701,-5.01389051,-5.215683432,-5.768735781,-5.995515809,-5.779290037,-5.418875813,-4.980214968,-4.693040929,-4.528137465,-4.428189044,-4.515509739,-4.378932219,-3.947500048,-3.559670434,-3.247083783,-3.005877862,-2.940577683,-3.002981229,-3.0846802,-3.202381415,-3.186795182,-3.214972844,-3.361116118,-3.509565988,-3.70648056,-3.855513906,-3.899519806,-3.833043957,-3.754152241,-3.682008629,-3.657242718,-3.644308009,-3.596854033,-3.565673156,-3.515118165,-3.518151922,-3.524660082,-3.523008218,-3.495294452,-3.438761778,-3.398089805,-3.317938523,-3.251707892,-3.195012898,-3.159144425,-3.120444489,-3.115701637,-3.113300753,-3.048922844,-2.979894042,-2.907006226,-2.870688062,-2.789443812,-2.683687301,-2.600151329,-2.522721548,-2.480790434,-2.494389682,-2.631874976,-2.737468414,-2.702020764,-2.594089746,-2.525913916,-2.432960472,-2.231961422,-2.00176784,-1.730077081,-1.437200537,-1.09760277,-0.82903012,-0.748379388,-0.791589711,-0.846024678,-0.963008881,-1.13162261,-1.219209261,-1.244567993,-1.23436518,-1.23944031,-1.264991524,-1.27023268,-1.213522613,-1.035702083,-0.632177761,-0.246632606,0.028409736,0.205202298,0.323157196,0.47830005,0.734551969,1.111569804,1.53066669,1.793914565,1.693493081,1.587437452,1.53140489,1.563106512,1.70700627,1.859331485,1.981780113,2.029987222,2.018128763,2.051342632,2.272769383,2.515661876,2.76573635,3.011280912,3.166466385,3.26582515,3.303400614,3.328598287,3.363487142,3.347043978,3.205975462,3.033770638,2.801492092,2.523119501,2.268061443,2.094674523,2.041400819,2.067906028,2.156896533,2.36798877,2.766144839,3.180624906,3.58822045,3.915554383,4.185843602,4.413812686,4.539859757,4.561895189,4.517007072,4.403463373,4.178325919,3.941944206,3.747669085,3.628293514,3.574139644,3.485756664,3.382109157,3.29066885,3.185892707,3.106281779,3.077370042,3.050773141,3.01667159,2.990119619,2.928393134,2.889594982,2.886035093,2.932455106,3.02237022,3.069248395,3.075435464,3.070353948,3.036869951,2.970173417,2.897237493,2.814106468,2.74201419,2.658271447,2.549926838,2.475800508,2.454043986,2.46554478,2.510441996,2.587594804,2.680901593,2.78120586,2.872030448,2.919085683,2.979101146,3.027040027,3.005302078,2.967390044,2.923445425,2.864082685,2.792580237,2.703141905,2.60504144,2.537222519,2.444360046,2.339480704,2.266312795,2.206762257,2.143344275,2.085939285,2.054745623,2.061290433,2.060717261,2.050142614,2.062155932,2.074391224,2.063605982,1.997220709,1.91864011,1.841773161,1.737597187,1.583099794,1.429848811,1.268254848,1.085375044,0.916889443,0.744949518,0.572939553,0.371373616,0.120465981,-0.128669145,-0.299634922,-0.371222758,-0.413382624,-0.475428732,-0.571163436,-0.676755079,-0.756964705,-0.810548176,-0.806145326,-0.809255463,-0.938879148,-1.283121517,-1.669555695,-1.957573352,-2.151785636,-2.23036875,-2.240681386,-2.210754349,-2.2084725,-2.219306688,-2.123632035,-1.88997117,-1.662299252,-1.600305858,-1.693828974,-1.918996329,-2.226765943,-2.575515127,-2.892095075,-3.153143673,-3.442510672,-3.744557962,-3.977150078,-4.074403005,-4.060290418,-3.955118666,-3.786184273,-3.558277392,-3.326348953,-3.101571908,-2.914277153,-2.724243298,-2.601737318,-2.473287935,-2.343174853,-2.296691451,-2.338695521,-2.470044813,-2.612865505,-2.761207066,-2.851945705,-2.86454021,-2.834289284,-2.879199595,-2.927764869,-2.903885574,-2.80986413,-2.647395196,-2.514422264,-2.411973748,-2.351688323,-2.500947647,-2.626053123,-2.628226237,-2.61905828,-2.593815317,-2.55015214,-2.547940464,-2.590965805,-2.686802545,-2.750658007,-2.570698814,-2.411515131,-2.351680841,-2.385284276,-2.524428,-2.661356006,-2.739480929,-2.643506389,-2.3779881,-2.086062202,-1.977740664,-1.91120028,-1.874001708,-1.734204283,-1.507821465,-1.345897341,-1.215804315,-1.218615379,-1.351086092,-1.527106671,-1.581608844,-1.586160042,-1.51606266,-1.467625697,-1.4403929,-1.390388105,-1.343370416,-1.24074604,-1.069478838,-0.880907998,-0.764036312,-0.687907014,-0.687137942,-0.737369227,-0.793703191,-0.818138847,-0.817022898,-0.829266508,-0.867987201,-0.902830896,-0.881625223,-0.762331882,-0.530675795,-0.237166345,0.04059535,0.242099829,0.355232918,0.442959077,0.561170337,0.705750811,0.849491134,0.931338706,0.961772232,0.960814688,0.9515606,0.96271733,1.032429261,1.096543846,1.104658723,1.102694972,1.070329177,1.0399332,1.022722778,1.055491898,1.130258154,1.243338778,1.344811579,1.477173852,1.636146263,1.77776606,1.919386696,2.016138221,2.058695341,2.039033335,2.033741387,2.054859859,2.067261378,2.022931103,1.909307162,1.749882516,1.612122861,1.557495345,1.536604959,1.532828553,1.472472323,1.363299552,1.310643687,1.343998551,1.488573267,1.6790167,1.829247243,1.874082813,1.878532058,1.885031706,1.952379516,2.049634199,2.125860137,2.149968629,2.081660455,1.958431054,1.838689005,1.786751217,1.774910384,1.727942509,1.578810064,1.388280489,1.196363149,1.057316585,1.004166403,0.999081948,0.972273893,0.891358107,0.804879649,0.78052752,0.859799427,0.9857016,1.09995667,1.150271384,1.160553863,1.205069479,1.286943933,1.385774941,1.493591769,1.590181895,1.650587797,1.617899859,1.549363778,1.485592757,1.363553205,1.161269876,0.95922622,0.854244678,0.764813339,0.654360461,0.56668169,0.598803527,0.678312263,0.736838491,0.851727705,1.000036998,1.132138546,1.15288258,1.125997276,1.098458245,1.045765554,0.995731514,0.966978585,1.015502795,1.06952566,1.137156283,1.223080151,1.325673544,1.430203744,1.52394564,1.596503436,1.57196614,1.496508031,1.399226105,1.327900735,1.277747769,1.281529701,1.344103908,1.446113997,1.580107011,1.719028909,1.92253091,2.154598431,2.315711151,2.464541783,2.61141628,2.623602886,2.566684447,2.5174504,2.476546406,2.492053343,2.513234895,2.541048867,2.665198585,2.76489987,2.859497093,3.030283747,3.217053734,3.343869824,3.434692355,3.514228556,3.560356325,3.603333237,3.613194475,3.609535856,3.554961078,3.535383857,3.5522052,3.610406662,3.683100891,3.729373358,3.781814935,3.786433736,3.76729423,3.728573136,3.675943696,3.531490834,3.304591348,3.055490069,2.825539696,2.598090796,2.371225624,2.184965186,2.029956002,1.899174702,1.811532552,1.788727317,1.842632173,1.890482059,1.87495441,1.799932846,1.665735806,1.471990632,1.193186839,0.914988808,0.617403346,0.271786702,-0.1302612,-0.507071034,-0.832979916,-1.106955449,-1.327407797,-1.517145156,-1.653756605,-1.79120275,-1.898401955,-1.950152155,-1.950934868,-2.002868885,-2.126573951,-2.286379492,-2.467863801,-2.684218554,-2.906619577,-3.128688231,-3.298808627,-3.417244191,-3.541440744,-3.6378159,-3.65742007,-3.669362521,-3.708836684,-3.692665253,-3.684852014,-3.667701802,-3.717754364,-3.843100452,-3.972746096,-3.972128267,-3.95940094,-3.93578321,-3.764515929,-3.61443521,-3.42909019,-3.180776453,-3.013361859,-2.963988414,-2.955687332,-3.141120381,-3.362475562,-3.462881708,-3.656813126,-3.765727143,-3.849621325,-4.049787726,-4.149668903,-4.030885363,-3.834257274,-3.574979439,-3.373596954,-3.318504443,-3.25650867,-3.258097506,-3.315512056,-3.304105091,-3.286558113,-3.358956575,-3.498916063,-3.599898129,-3.592935925,-3.504448667,-3.400162792,-3.286361098,-3.141048388,-3.035082989,-2.940144958,-2.881097732,-2.787440224,-2.708204527,-2.64018786,-2.639874239,-2.689997144,-2.77721221,-2.868801646,-2.902046294,-2.889838724,-2.834313874,-2.817815113,-2.82762095,-2.903993034,-2.910089121,-2.857651763,-2.729302092,-2.554247198,-2.392134328,-2.324187918,-2.292518544,-2.198187168,-2.086439254,-1.919319932,-1.752595813,-1.580402453,-1.432586212,-1.333960962,-1.259143889,-1.133086574,-0.95661613,-0.812545278,-0.680619822,-0.573617134,-0.546330483,-0.588143728,-0.668098092,-0.732456179,-0.747451248,-0.724401574,-0.672799683,-0.529669802,-0.286167264,-0.023073142,0.252067571,0.496600888,0.686753981,0.785551109,0.795028648,0.8171786,0.876068861,0.85490782,0.658103006,0.403559713,0.178692203,0.061725528,0.069336329,0.244639101,0.476717528,0.574452689,0.512275864,0.441786518,0.551370951,0.789342506,1.084008006,1.358059684,1.604138921,1.767647649,1.954660665,2.212901489,2.487303193,2.704623792,2.815637509,2.840011426,2.814160816,2.768951201,2.647063736,2.434719067,2.119490752,1.812809318,1.629683393,1.571498011,1.682550619,1.964357236,2.290767375,2.558880664,2.846943236,3.178797224,3.521283749,3.789379066,3.983999615,4.065034379,3.879831537,3.383799441,2.871509551,2.556660269,2.313734642,2.179092821,2.156536428,2.242721174,2.270049233,2.345385227,2.536708101,2.913331431,3.242032015,3.425696113,3.614071976,3.725065781,3.805709848,3.8612172,3.954121758,3.995683778,3.987795558,3.885687236,3.774232215,3.618071957,3.425417514,3.273826371,3.13035599,2.988043197,2.913708253,2.935265378,3.022011251,3.143904103,3.238760523,3.359533037,3.425819064,3.430230492,3.381599318,3.308797204,3.198214601,3.045575348,2.874752225,2.742480006,2.685498999,2.647784509,2.652530504,2.663827415,2.666589798,2.647521272,2.608540009,2.541029359,2.468135481,2.354645613,2.233074564,2.134363845,2.025311875,1.914627084,1.798593956,1.69408081,1.577649901,1.464644088,1.325599262,1.181479488,0.989450576,0.72511571,0.42262688,0.116044812,-0.157533162,-0.415670919,-0.673696272,-0.939601339,-1.231070976,-1.553487943,-1.837028472,-2.016484338,-2.152070074,-2.245507222,-2.356250484,-2.462283976,-2.533218365,-2.562428833,-2.50971545,-2.393233311,-2.350723572,-2.408224511,-2.389687314,-2.414706836,-2.431118624,-2.517885818,-2.663638997,-2.860966282,-3.04455852,-3.21403265,-3.213099251,-3.139034138,-3.180820184,-3.180593677,-3.197228203,-3.173318701,-3.057790093,-2.854450827,-2.767193084,-2.711964316,-2.802221847,-2.96198204,-3.099264174,-3.178061833,-3.23594718,-3.181919374,-3.192140188,-3.296677408,-3.232284393,-3.154828439,-3.015696216,-2.772902002,-2.539976206,-2.576051559,-2.538564477,-2.625141721,-2.691150866,-2.708797722,-2.875790472,-3.033602767,-3.27516542,-3.626170688,-3.879886684,-3.735905375,-3.644966602,-3.489169788,-3.355851269,-3.231104016,-3.070104938,-2.948706827,-2.749998107,-2.465462151,-2.22187089,-2.241759653,-2.31062037,-2.424586029,-2.508909383,-2.598965631,-2.644457154,-2.627885633,-2.58512938,-2.593003035,-2.654712663,-2.652500725,-2.620938921,-2.530592384,-2.39125432,-2.245679851,-2.148965592,-2.103829403,-2.153715324,-2.175380211,-2.133109512,-2.079621773,-1.945525403,-1.819184184,-1.774616208,-1.742973828,-1.658891358,-1.601573677,-1.507713666,-1.456825976,-1.500765963,-1.559795532,-1.757790403,-2.037532563,-2.255317063,-2.33928266,-2.380736089,-2.295346022,-2.111406798,-1.884018755,-1.588827682,-1.351586914,-1.076094122,-0.744617188,-0.477701149,-0.406197941,-0.45970088,-0.598348105,-0.816129675,-1.017871776,-1.213084431,-1.33067106,-1.412333283,-1.499414771,-1.553393126,-1.515048087,-1.405290875,-1.297133574,-1.187958806,-1.129691784,-1.06735368,-0.972136977,-0.883407178,-0.804617295,-0.7368155,-0.665390925,-0.616383564,-0.604083273,-0.61482012,-0.555750508,-0.492113979,-0.490569575,-0.468377864,-0.392101669,-0.270248859,-0.191312416,-0.132395918,-0.025599458,0.121220167,0.200951903,0.225900106,0.238244443,0.204827843,0.105620022,-0.064759407,-0.242116313,-0.405373561,-0.595507987,-0.819314716,-0.991924851,-1.059021304,-0.937261384,-0.710073545,-0.428464544,-0.144275954,0.154224634,0.429078052,0.705628889,0.96323926,1.162591953,1.291489351,1.162596977,0.942556343,0.716487567,0.569443028,0.449155314,0.373562825,0.287538345,0.248932664,0.229290092,0.153833382,0.163089845,0.183253,0.180996609,0.139738958,0.101925235,0.061213524,0.084314597,0.10605842,0.132544773,0.198045679,0.254874644,0.294956635,0.310205197,0.312570952,0.323572323,0.412047479,0.529723697,0.718628139,1.003053489,1.345807633,1.624519565,1.777521944,1.84212074,1.85385674,1.831982315,1.707420276,1.51902868,1.21119405,0.728932843,0.116767726,-0.370078821,-0.512209835,-0.329753811,-0.013814212,0.323347039,0.623333893,0.871926361,1.098322761,1.380721983,1.762539351,2.061546813,2.044180644,1.731102813,1.404148543,1.142056156,1.025999655,0.981995412,1.016558472,1.064104057,1.091450936,1.209823325,1.468757843,1.815484553,2.061751938,2.218133321,2.264887176,2.283456667,2.308206909,2.363279413,2.463217657,2.497602172,2.458923985,2.406203582,2.416732109,2.507575864,2.684158494,2.876653266,2.997440682,3.076695918,3.086018214,3.059425536,3.092652146,3.09917578,3.070541982,2.964573422,2.821804259,2.697739062,2.692965655,2.713495789,2.809639761,2.984838471,3.162948383,3.454280473,3.760617889,4.039403998,4.236735638,4.392896161,4.432564704,4.48984154,4.47557034,4.456958949,4.400474507,4.24343989,4.090936829,3.986959168,3.923170544,3.869484504,3.873335422,3.885843067,3.916108283,3.931648629,3.985672375,4.032116067,4.038219794,4.008900785,3.95907064,3.900199013,3.833971394,3.739858656,3.689761766,3.619171479,3.488673176,3.364860753,3.254611483,3.163595921,3.109136972,3.07286019,3.044216244,3.021161927,2.957747295,2.90052406,2.868436012,2.850626172,2.837845115,2.825266605,2.815378558,2.793702677,2.731354281,2.632890158,2.517056727,2.387628282,2.265630583,2.146405132,2.037144015,1.916372667,1.746683888,1.535531262,1.322460069,1.146143361,0.988120138,0.815452111,0.636751217,0.45076073,0.255479176,0.067516201,-0.081664293,-0.163121126,-0.219344068,-0.338832991,-0.514047253,-0.712948205,-0.951358612,-1.196434253,-1.393380337,-1.534566426,-1.656932559,-1.779642973,-1.903305056,-1.986001387,-2.051941655,-2.153043323,-2.234733298,-2.296892805,-2.367627513,-2.375557003,-2.42867867,-2.504486442,-2.597907603,-2.712020888,-2.747545805,-2.563479986,-2.249002957,-1.965436721,-1.782735972,-1.882110796,-1.969113832,-2.108205194,-2.252944188,-2.349179268,-2.528618131,-2.939164839,-3.516957345,-3.940588131,-4.308180051,-4.502688556,-4.521107106,-4.465877638,-4.375725055,-4.324467955,-4.276668291,-4.006496644,-3.592206068,-3.426457148,-3.257166238,-3.10207922,-3.181913834,-3.347644796,-3.497659888,-3.575041585,-3.504238791,-3.625223956,-3.886397448,-4.016485391,-4.118327179,-4.141370511,-4.135308971,-3.997744327,-3.912305665,-3.891234446,-3.90306591,-3.872879534,-3.844031138,-3.855659528,-3.838555207,-3.789096413,-3.656757779,-3.569391809,-3.422906094,-3.285609365,-3.385749083,-3.490793633,-3.497722168,-3.399391117,-3.326065907,-3.428472285,-3.619966469,-3.854619694,-4.037258296,-4.010260024,-3.684826446,-3.274957504,-2.865462251,-2.548280087,-2.348105545,-2.140378575,-1.842106662,-1.513958697,-1.346461296,-1.473844123,-1.715135031,-1.972509127,-2.2545296,-2.511633329,-2.615473518,-2.536825418,-2.429075842,-2.318907118,-2.154336586,-1.928290796,-1.75794723,-1.641889524,-1.531779521,-1.418006308,-1.337385495,-1.367589395,-1.475352051,-1.55917558,-1.537264087,-1.330190804,-0.986980023,-0.626571996,-0.283293476,0.016358402,0.276591752,0.537779818,0.806340387,1.02955615,1.149849784,1.092679541,0.931746074,0.843265526,0.847224082,0.949765431,1.0747894,1.164780014,1.254698794,1.371456792,1.535399735,1.773208969,2.04560061,2.210512058,2.266683472,2.229080308,2.266983535,2.349878212,2.436993631,2.468301396,2.439628038,2.339521597,2.20810173,2.152991465,2.182838658,2.351252468,2.39560402,2.464744082,2.516882815,2.622548441,2.774663042,3.001571739,3.229187198,3.410184702,3.53911823,3.543235523,3.597139974,3.621318796,3.67351553,3.717387142,3.737000637,3.697039776,3.667560414,3.662544588,3.647933493,3.627528887,3.621766862,3.623137414,3.650156801,3.692302483,3.756714753,3.86206178,3.957380328,4.008451623,4.041277965,4.082139028,4.094783499,4.100636656,4.055819293,3.988705025,3.91885303,3.831498818,3.742204998,3.672818838,3.632680473,3.612894189,3.584589735,3.526784707,3.466031744,3.398842897,3.30212988,3.18870554,3.07959653,2.963180044,2.824349948,2.65802847,2.498135203,2.36611394,2.250295618,2.123051395,2.010550329,1.921432377,1.823830632,1.72408032,1.617786481,1.507824089,1.395527579,1.263625143,1.109645665,0.959457037,0.797331716,0.613996624,0.420483644,0.210087774,-0.001250889,-0.199445701,-0.394193892,-0.557821383,-0.667926297,-0.762550498,-0.824314629,-0.84844867,-0.852700076,-0.820600114,-0.775877328,-0.77181577,-0.837766461,-0.983404133,-1.17955837,-1.376877029,-1.610009863,-1.927662511,-2.230623083,-2.54805686,-2.831077771,-3.011667943,-3.020906196,-2.919513383,-2.852810163,-2.854359956,-2.87685864,-2.818906641,-2.778305254,-2.705466561,-2.637536201,-2.599357514,-2.670568519,-2.793391099,-2.83823113,-2.765081644,-2.632998762,-2.495264301,-2.348218856,-2.210840058,-2.106031237,-2.040825171,-2.011315613,-2.055086203,-2.185792527,-2.375600743,-2.523558044,-2.654087276,-2.763436918,-2.901799994,-2.980329909,-2.977579484,-2.926764388,-2.830835185,-2.689983082,-2.581188574,-2.59597321,-2.727013779,-2.900220723,-2.939152031,-2.916960173,-2.9587467,-2.983306007,-2.887090549,-2.730119643,-2.544510555,-2.35827003,-2.150876422,-1.938877311,-1.950548511,-2.162137389,-2.295179086,-2.420226693,-2.671149001,-2.910817204,-3.01380033,-2.933935142,-2.729750843,-2.511155362,-2.170377426,-1.737585511,-1.485037637,-1.322098827,-1.151445398,-1.070954008,-1.158112283,-1.337654881,-1.545048809,-1.759238033,-2.060467949,-2.325148978,-2.375099926,-2.31303432,-2.231230116,-2.227215452,-2.243369932,-2.291070447,-2.277905636,-2.215261655,-2.021142635,-1.88268292,-1.999603143));

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
        saveBtn.setOnClickListener(new View.OnClickListener() {
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
        });
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
        }, 10000);
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
                   // writer2.close();
                    writer3.close();
                    writer4.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(4000);

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
          //  writer2 = new FileWriter(new File(path,"STDTurn"+ filenumber + ".csv"));

            writer3 = new FileWriter(new File(path,"GyroScope"+ filenumber + ".csv"));

            writer4 = new FileWriter(new File(path,"NormalXYZV"+ filenumber + ".csv"));

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
        if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
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
            if (listEuclidNormSample.size() > 11 && thresholdExist) {

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
                    vView.setText("|v| turns: " + countRegulationEuclid);
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
                    yView.setText("y turns: " + countRegulationY);
                } else if (calculateAverage(sampleYNew) > thresholdSkiiY && thresholdSkiiY < calculateAverage(sampleYOld)
                        && longestTurnY > 500 && Math.abs(calculateAverage(sampleYNew)) - Math.abs(calculateAverage(sampleYOld)) < 0.2) {
                    longestTurnY = 0;
                    countRegulationY++;
                    yView.setText("y turns: " + countRegulationY);
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

                double yDifference = maxY - (minY);
                double xDifference = maxX - (minX);


                double amountTurns;
                if (yDifference > xDifference) {
                    stdDevs = yDifference;
                    amountTurns = countRegulationY;

                } else {
                    stdDevs = xDifference;
                    amountTurns = countRegulationX;
                }
                //clock, deviant,amount of turns,
            /*    try {
                    writer2.write((System.currentTimeMillis()- initialTime) + "," + Double.toString(stdDevs) +
                            "," + amountTurns + "," // + mCurrentLocation.getSpeed()
                            + "\n");
                    writer2.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } */

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
