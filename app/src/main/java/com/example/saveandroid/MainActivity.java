package com.example.saveandroid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;

import android.media.MediaPlayer;
import android.net.Uri;

import android.os.Build;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Telephony;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.view.MenuItem;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.flaviofaria.kenburnsview.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import CameraApp.BackCameraService;
import CameraApp.CameraService;
import CameraApp.CrashService;
import CameraApp.CustomDialogBox;

import CameraApp.EmotionService;
import CameraApp.FatigueService;
import CameraApp.FloatingIcon;
import CameraApp.FrontCameraService;
import CameraApp.PedestrianService;
import CameraApp.rPPGService;
import FaceDetector.FaceDetectionActivity;
import SpeechRecognition.Speech;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import static android.Manifest.permission.RECORD_AUDIO;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, SharedPreferences.OnSharedPreferenceChangeListener, RecognitionListener {
    public static final String TAG = "MAIN ACTIVITY";
    private static final int TTS_CHECK_CODE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 200;
    private static final int CALL_PERMISSION = 200;
    public static int totalFear;
    public static int totalHappy;
    public static int totalSad;
    public static int totalAngry;
    public static int totalFatigue;
    static boolean preferencesChanged=false;
    MediaPlayer player;
    public static WeakReference<MainActivity> weakMainActivity;

    public static TextToSpeech tts;
    private KenBurnsView kbv;
    private boolean moving = true;
    boolean cameraBounded;
    boolean frontCameraBounded;
    boolean emotionBounded;
    boolean fatigueBounded;
    boolean pedestrianBounded;
    boolean rPPGBounded;
    boolean speechBounded;
    CameraService cameraServer;
    FrontCameraService frontCameraServer;
    EmotionService emotionServer;
    FatigueService fatigueServer;
    PedestrianService pedestrianServer;
    rPPGService rPPGServer;
    Speech speechServer;
    public static boolean textToSpeechIsInitialized = false;
    public static SpeechRecognizer speechRecognizer;
    public static Intent intentRecognizer;
    public static String speechString = "";
    public Intent callIntent;
    public Intent smsIntent;

    public static boolean fatigueStarted;
    public static boolean emotionStarted;
    public static boolean rPPGStarted;
    public static boolean crashStarted;

    // SPOTIFY VARIABLES
    private static final String CLIENT_ID = "b35e3998a3fa4e3e88f85a78124e422a"; // SPOTIFY CLIENT ID
    private static final String REDIRECT_URI = "https://utkukalkanli319.github.io/SAVE/";
    private SpotifyAppRemote mSpotifyAppRemote;
    public static boolean playHappyPlaylist;
    public static boolean playEnergeticPlaylist;
    public static boolean playCalmPlaylist;
    public static boolean isPlayingMusic;
    public static boolean openMapFatigue;
    public static boolean openMapRPPG;
    public static String lastPlayedGenre;

    // SPEECH TO TEXT VARIABLES
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private TextView returnedError;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String TAG_SPEECH = "VoiceRecognitionMainActivity";

    private boolean isInHighPulse;
    private CustomDialogBox customDialogBox = null;
    private static FloatingIcon floatingIcon = null;

    Connection connect;
    String connectionResult = "";

    boolean drowsinessVal;
    boolean popupVal;
    boolean rppgVal;
    boolean crashVal;
    boolean emotionVal;
    boolean audioVal;
    boolean sesliCevapVal;

    public static MainActivity getInstanceActivity() {
        return weakMainActivity.get();

        /*        try {
            return weakMainActivity.get();
        }
        catch (Exception exception){
            Log.e(TAG,"Week reference null pointer exception");
        }
        return null ;*/
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service.getClass().getName().equals(CameraService.LocalBinder.class.getName())) {
                onServiceConnected1(name, (CameraService.LocalBinder) service);
            } else if (service.getClass().getName().equals(EmotionService.LocalBinder.class.getName())) {
                onServiceConnected2(name, (EmotionService.LocalBinder) service);
            } else if (service.getClass().getName().equals(FatigueService.LocalBinder.class.getName())) {
                onServiceConnected3(name, (FatigueService.LocalBinder) service);
            } else if (service.getClass().getName().equals(PedestrianService.LocalBinder.class.getName())) {
                onServiceConnected4(name, (PedestrianService.LocalBinder) service);
            } else if (service.getClass().getName().equals(rPPGService.LocalBinder.class.getName())) {
                onServiceConnected5(name, (rPPGService.LocalBinder) service);
            } else if (service.getClass().getName().equals(FrontCameraService.LocalBinder.class.getName())) {
                onServiceConnected6(name, (FrontCameraService.LocalBinder) service);
            } else if (service.getClass().getName().equals(CrashService.LocalBinder.class.getName())) {
                onCrashServiceConnected(name, (CrashService.LocalBinder) service);
            } else if (service.getClass().getName().equals(Speech.LocalBinder.class.getName())) {
                onServiceConnectedSpeech(name, (Speech.LocalBinder) service);
            }
        }

        public void onServiceConnected1(ComponentName name, CameraService.LocalBinder service) {
            System.out.println("CONNECTED");
            cameraBounded = true;
            CameraService.LocalBinder mLocalBinder = service;
            cameraServer = mLocalBinder.getServerInstance();
        }

        public void onServiceConnected2(ComponentName name, EmotionService.LocalBinder service) {
            System.out.println("CONNECTED");
            emotionBounded = true;
            EmotionService.LocalBinder mLocalBinder = service;
            emotionServer = mLocalBinder.getServerInstance();
        }

        public void onServiceConnected3(ComponentName name, FatigueService.LocalBinder service) {
            System.out.println("CONNECTED");
            fatigueBounded = true;
            FatigueService.LocalBinder mLocalBinder = service;
            fatigueServer = mLocalBinder.getServerInstance();
        }

        public void onServiceConnected4(ComponentName name, PedestrianService.LocalBinder service) {
            System.out.println("CONNECTED");
            pedestrianBounded = true;
            PedestrianService.LocalBinder mLocalBinder = service;
            pedestrianServer = mLocalBinder.getServerInstance();
        }

        public void onServiceConnected5(ComponentName name, rPPGService.LocalBinder service) {
            System.out.println("CONNECTED");
            rPPGBounded = true;
            rPPGService.LocalBinder mLocalBinder = service;
            rPPGServer = mLocalBinder.getServerInstance();
        }

        public void onServiceConnected6(ComponentName name, FrontCameraService.LocalBinder service) {
            System.out.println("CONNECTED");
            frontCameraBounded = true;
            FrontCameraService.LocalBinder mLocalBinder = service;
            frontCameraServer = mLocalBinder.getServerInstance();
        }

        private void onCrashServiceConnected(ComponentName name, CrashService.LocalBinder service) {
            Log.d(TAG, "onCrashServiceConnected");
        }

        public void onServiceConnectedSpeech(ComponentName name, Speech.LocalBinder service) {
            System.out.println("Speech Connected");
            speechBounded = true;
            Speech.LocalBinder mSpeech = service;
            speechServer = mSpeech.getServerInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, " onServicedDisconnected");
            // set bounded booleans to false
            cameraBounded = false;
            frontCameraBounded = false;
            emotionBounded = false;
            fatigueBounded = false;
            pedestrianBounded = false;
            rPPGBounded = false;
            speechBounded = false;
            // disconnect servers
            cameraServer = null;
            frontCameraServer = null;
            emotionServer = null;
            fatigueServer = null;
            pedestrianServer = null;
            rPPGServer = null;
            speechServer = null;
        }
    };

    /*
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int hiz = intent.getIntExtra("hiz", 0);
        }
    };


     */

    public static String startSpeechElif(){
        try {
            speechRecognizer.startListening(intentRecognizer);
            Log.i(TAG, "START SPEECH KISMINDAYIZ " + speechString);
            return speechString;
        }
        catch(Exception e){
            Log.e(TAG, "Speech cannot be started " +e);
        }
        return "Could not read";
    }

    public static void stopSpeech(){
        speechRecognizer.stopListening();
    }

    public static String getSpeech(){
        return speechString;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        drowsinessVal = sharedPreferences.getBoolean("drowsiness", true);
        popupVal = sharedPreferences.getBoolean("popup", true);
        rppgVal = sharedPreferences.getBoolean("rppg", true);
        crashVal = sharedPreferences.getBoolean("gforce", true);
        emotionVal = sharedPreferences.getBoolean("music", true);
        audioVal = sharedPreferences.getBoolean("audio", true);
        sesliCevapVal = sharedPreferences.getBoolean("voiceCommand", true);

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate enter");

        startKenBurnsView(); // start special ken burns view

        playHappyPlaylist = false;
        playCalmPlaylist = false;
        playEnergeticPlaylist = false;
        isPlayingMusic = false;
        weakMainActivity = new WeakReference<>(MainActivity.this);
        lastPlayedGenre = "";

        fatigueStarted = false;
        emotionStarted = false;
        crashStarted = false;
        rPPGStarted = false;

        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(getApplicationContext()));
            totalFear = obj.getInt("totalfear");
            totalHappy = obj.getInt("totalhappy");
            totalAngry = obj.getInt("totalangry");
            totalSad = obj.getInt("totalsad");
            totalFatigue = obj.getInt("totalfatigue");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechIsInitialized = true;
                    int r = tts.setLanguage(Locale.US);
                    if (r == TextToSpeech.LANG_MISSING_DATA
                            || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "This Language is not supported");
                    }
                } else if (status == TextToSpeech.ERROR) {
                    Log.e(TAG, "Something went wrong for speech");
                }
            }
        });
        if (tts != null) {
            Log.i(TAG, "TTTS NULLLLLLL HEEEEEEEEEY");
            Log.i(TAG, "TTTS NULLLLLLL HEEEEEEEEEY");
            if (textToSpeechIsInitialized) {
                tts.speak("Trying", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            Log.i(TAG, "Null degil");
        } else {
            Log.e(TAG, "Activity mainde de calismadi");
        }

        // SPEECH TO TEXT
        resetSpeechRecognizer();

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        setRecognizerIntent();
        //speech.startListening(recognizerIntent);

        checkPermissions();
        // SETTINGS
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }
    // utku
    private void resetSpeechRecognizer(){
        if(speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(TAG_SPEECH, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if(SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }
    // utku
    private void setRecognizerIntent(){
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    // ELIF
    private void setSpeechRecognizerElif(){
        // SPEECH TO TEXT

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //  intentRecognizer.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.i(TAG, "ON RESULTS SIZE " + matches.size());
                String s = "";
                if (matches != null) {
                    for (int i = 0; i < matches.size(); i++) {
                        s += matches.get(i);
                    }
                }
                speechString = s;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String s = "";
                if (matches != null) {
                    for (int i = 0; i < matches.size(); i++) {
                        s += matches.get(i);
                    }
                }
                speechString = s;
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }



    public void checkPermissions(){
        // PERMISSION CHECK FOR CAMERA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        // PERMISSION CHECK FOR MIC
        if (ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, REQUEST_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION);
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter("cs_Message"));


        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Spofity connected! Yay!");
                        // Now you can start interacting with App Remote
                        //connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
        //  makeCall("");
        Log.i(TAG, " after make call");

    }


    public void jukeBox(String playlistType){
        if(!playlistType.equals(lastPlayedGenre) || !isPlayingMusic){
            if(playlistType.equals("Uplift")){
                //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:7mVs8kAaSgHEYaM2g2sRM1");
                isPlayingMusic = true;
                lastPlayedGenre = "Uplift";
            }
            else if(playlistType.equals("Energetic")){
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:5UG3L9ClOde17mwIRg4W11"); // this should change
                isPlayingMusic = true;
                lastPlayedGenre = "Energetic";
            }
            else if(playlistType.equals("Calm")){
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX7K31D69s4M1");
                isPlayingMusic = true;
                lastPlayedGenre = "Calm";
            }
            else if(playlistType.equals("Fear")){
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:52SCQMfvDpqfGfRtGdj9qA");
                isPlayingMusic = true;
                lastPlayedGenre = "Fear";
            }
            else if(playlistType.equals("Happy")){
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
                isPlayingMusic = true;
                lastPlayedGenre = "Happy";
            }
        }
        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

    // from spotify documentation wont be used
    public void connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }
    /**
    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
     */


    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance

            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }
     */

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void activateRoadTrip(View view) {
        emotionStarted = false;
        rPPGStarted = false;
        fatigueStarted = true;
        crashStarted = false;

        Log.i(TAG, " ACTIVATE ROAD");
        //Toast.makeText(getApplicationContext(), "activating road trip", Toast.LENGTH_LONG).show();

        Intent speechIntent = new Intent(MainActivity.this, Speech.class);
        bindService(speechIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(speechIntent);
        /*
        SharedPreferences preferences = getSharedPreferences("root_settings", MODE_PRIVATE);
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.i(TAG, "bunlar var: " + preferences.getAll());

        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.i(TAG, getApplicationContext().toString());
        boolean value1 = preferences.getBoolean("audio", false);
        Log.i(TAG, "Audio: " + value1);
        boolean value2 = preferences.getBoolean("music", false);
        Log.i(TAG, "Music: " + value2);

         */


        Intent frontCameraIntent = new Intent(MainActivity.this, FrontCameraService.class);
        bindService(frontCameraIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(frontCameraIntent);
        if(emotionStarted && emotionVal){
            Intent emotionIntent = new Intent(MainActivity.this, EmotionService.class);
            bindService(emotionIntent, serviceConnection, BIND_AUTO_CREATE);
            MainActivity.this.startService(emotionIntent);
            emotionStarted = true;
        }
        if(rPPGStarted && rppgVal){
            Intent rPPGIntent = new Intent(MainActivity.this, rPPGService.class);
            bindService(rPPGIntent, serviceConnection, BIND_AUTO_CREATE);
            MainActivity.this.startService(rPPGIntent);
            rPPGStarted = true;
        }
        if(fatigueStarted && drowsinessVal){
            Intent fatigueIntent = new Intent(MainActivity.this, FatigueService.class);
            bindService(fatigueIntent, serviceConnection, BIND_AUTO_CREATE);
            MainActivity.this.startService(fatigueIntent);
            fatigueStarted = true;
        }
        if(crashStarted && crashVal){
            startTracking(null);
            crashStarted = true;
        }
        Speech.readText("Starting our road trip");
        //sendSMS("+905077907940", "Check Utku !! ");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTracking(View view) {
        CameraApp.LocationTrackerService.StartLocationTracking(getApplicationContext());
        Log.i(TAG, " start location tracking");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void playAlarm(){
        if(player == null){
            player = MediaPlayer.create(this, R.raw.alarm);
            player.setOnCompletionListener(mp -> stopPlayer());
        }
        player.start();
    }
    private void stopPlayer(){
        if (player != null){
            player.release();
            player = null;
        }
    }


    public void startKenBurnsView() {
        int colorCodeDark = Color.parseColor("#FF9800");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colorCodeDark);
        setContentView(R.layout.nav_activity_main);

        kbv = findViewById(R.id.kbv);

        // UI initialisation
        returnedText = findViewById(R.id.textView1);
        //returnedError = findViewById(R.id.errorView1);
        progressBar =  findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);

        AccelerateDecelerateInterpolator adi = new AccelerateDecelerateInterpolator();
        RandomTransitionGenerator generator = new RandomTransitionGenerator(4000, adi);
        kbv.setTransitionGenerator(generator);

        kbv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moving) {
                    kbv.pause();
                    moving = false;
                } else {
                    kbv.resume();
                    moving = true;
                }
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.nav_home) {
                            //Toast.makeText(MainActivity.this,  "home", Toast.LENGTH_SHORT).show();
                            Intent addPetIntent = new Intent(MainActivity.this, SettingsActivity.class);
                            MainActivity.this.startActivity(addPetIntent);
                        } else if (item.getItemId() == R.id.nav_gallery) {
                            //Toast.makeText(MainActivity.this,  "home", Toast.LENGTH_SHORT).show();
                            Intent addPetIntent = new Intent(MainActivity.this, PrefActivity.class);
                            MainActivity.this.startActivity(addPetIntent);
                        } else if ( item.getItemId() == R.id.nav_slideshow){
                            Intent addPetIntent = new Intent(MainActivity.this, UserData.class);
                            MainActivity.this.startActivity(addPetIntent);
                        }
                        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

        kbv.setTransitionListener(new KenBurnsView.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                //Toast.makeText(MainActivity.this,"Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                //Toast.makeText(MainActivity.this,"Finished", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openGoogleMaps(String location){
        if ( location.equals("hospital")){
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=hospitals");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else if ( location.equals("station")){
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=gas+station");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else if ( location.equals("cafe")){
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=cafe");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
        else {
            Log.e(TAG,"Location for map is invalid");
        }
    }

    public void makeCall(String telNo, String message){
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + telNo)); // Must be android

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},CALL_PERMISSION);

            Log.e(TAG,"Permission denied for call");
        } else {
            //You already have permission
            try {
                // callIntent = new Intent(Intent.ACTION_DIAL);
                startActivity(callIntent);

                Log.i(TAG,"Phone call is made");
                Thread.sleep(500);
                Speech.readText(message);
            } catch(SecurityException | InterruptedException e) {
                e.printStackTrace();
                Log.i(TAG,"Call failed." + e);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(sharedPreferences !=null){
            preferencesChanged=true;
        }
    }

    public void putData(){
        try {
            Database database = new Database();
            connect = database.connection();
            if ( connect != null ){
                // put data
            }
        }
        catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("a.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void sendSMS(String number, String message){
        android.telephony.SmsManager sms=android.telephony.SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, null, null);
    }

    public static String getDefaultSmsAppPackageName(@NonNull Context context) {
        String defaultSmsPackageName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
            return defaultSmsPackageName;
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_DEFAULT).setType("vnd.android-dir/mms-sms");
            final List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
            if (resolveInfos != null && !resolveInfos.isEmpty())
                return resolveInfos.get(0).activityInfo.packageName;

        }
        return null;
    }

    // UTKU
    public void startSpeech(){
        speech.startListening(recognizerIntent);
    }

    public String getSpeechString(){
        return speechString;
    }

    @Override
    public void onResume() {
        Log.i(TAG_SPEECH, "resume");
        super.onResume();
        resetSpeechRecognizer();
    }

    @Override
    protected void onPause() {
        Log.i(TAG_SPEECH, "pause");
        super.onPause();
        speech.stopListening();
    }

    @Override
    protected void onStop() {
        Log.i(TAG_SPEECH, "stop");
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
        speech.stopListening();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG_SPEECH, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG_SPEECH, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG_SPEECH, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        speech.stopListening();
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(TAG_SPEECH, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        returnedText.setText(text);
        progressBar.setIndeterminate(false);
        speechString = text;
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(TAG_SPEECH, "FAILED " + errorMessage);
        //returnedError.setText(errorMessage);
        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(TAG_SPEECH, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(TAG_SPEECH, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(TAG_SPEECH, "onReadyForSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}