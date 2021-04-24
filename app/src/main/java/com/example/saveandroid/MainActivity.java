package com.example.saveandroid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONObject;

import android.view.MenuItem;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.flaviofaria.kenburnsview.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import CameraApp.CameraService;
import CameraApp.CrashService;
import CameraApp.EmotionService;
import CameraApp.FatigueService;
import CameraApp.FrontCameraService;
import CameraApp.PedestrianService;
import CameraApp.rPPGService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private static final String TAG = "cs_mainactivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;
    private KenBurnsView kbv;
    private boolean moving = true;
    static Bitmap data;
    boolean cameraBounded;
    boolean frontCameraBounded;
    boolean emotionBounded;
    boolean fatigueBounded;
    boolean pedestrianBounded;
    boolean rPPGBounded;
    CameraService cameraServer;
    FrontCameraService frontCameraServer;
    EmotionService emotionServer;
    FatigueService fatigueServer;
    PedestrianService pedestrianServer;
    rPPGService rPPGServer;

    public static double gazeAngle = 0;
    public static double headPose = 0;
    //String url = "http://" + "192.168.1.20" + "/" + "predict";
    private java.net.URL URL;
   // private String url = "http://" + "10.0.0.2" + "/" + "predict"; // try 10.0.0.2
   // private String url = "http://" + "10.0.0.2" + "/" + "predict"; // try 10.0.0.2
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Response response;
    File file;
    MediaType JSON;
    TextView hizView;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(service.getClass().getName().equals(CameraService.LocalBinder.class.getName())){
                onServiceConnected1(name, (CameraService.LocalBinder) service);
            }
            else if(service.getClass().getName().equals(EmotionService.LocalBinder.class.getName())){
                onServiceConnected2(name, (EmotionService.LocalBinder) service);
            }
            else if(service.getClass().getName().equals(FatigueService.LocalBinder.class.getName())){
                onServiceConnected3(name, (FatigueService.LocalBinder) service);
            }
            else if(service.getClass().getName().equals(PedestrianService.LocalBinder.class.getName())){
                onServiceConnected4(name, (PedestrianService.LocalBinder) service);
            }
            else if (service.getClass().getName().equals(rPPGService.LocalBinder.class.getName())){
                onServiceConnected5(name, (rPPGService.LocalBinder) service);
            }
            else if(service.getClass().getName().equals(FrontCameraService.LocalBinder.class.getName())){
                onServiceConnected6(name, (FrontCameraService.LocalBinder) service);
            }
            else if(service.getClass().getName().equals(CrashService.LocalBinder.class.getName())) {
                onCrashServiceConnected(name, (CrashService.LocalBinder) service);
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
        public void onServiceConnected4(ComponentName name, PedestrianService.LocalBinder service){
            System.out.println("CONNECTED");
            pedestrianBounded = true;
            PedestrianService.LocalBinder mLocalBinder = service;
            pedestrianServer = mLocalBinder.getServerInstance();
        }
        public void onServiceConnected5(ComponentName name, rPPGService.LocalBinder service){
            System.out.println("CONNECTED");
            rPPGBounded = true;
            rPPGService.LocalBinder mLocalBinder = service;
            rPPGServer = mLocalBinder.getServerInstance();
        }
        public void onServiceConnected6(ComponentName name, FrontCameraService.LocalBinder service){
            System.out.println("CONNECTED");
            frontCameraBounded = true;
            FrontCameraService.LocalBinder mLocalBinder = service;
            frontCameraServer = mLocalBinder.getServerInstance();
        }

        private void onCrashServiceConnected(ComponentName name, CrashService.LocalBinder service) {
            Log.d(TAG, "onCrashServiceConnected");
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
            // disconnect servers
            cameraServer = null;
            frontCameraServer = null;
            emotionServer = null;
            fatigueServer = null;
            pedestrianServer = null;
            rPPGServer = null;
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int hiz = intent.getIntExtra("hiz", 0);
            hizView.setText("" + hiz);


            Log.d(TAG, "al:" + hiz);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate enter");
        startKenBurnsView(); // start special ken burns view
        hizView = findViewById(R.id.hizGoster);
        // PERMISSION CHECK FOR CAMERA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter("cs_Message"));
    }

    public void activateRoadTrip(View view){
        //Intent faceIntent = new Intent(MainActivity.this, FaceDetectionActivity.class);
        // MainActivity.this.startActivity(faceIntent);
        //Intent cameraIntent = new Intent(MainActivity.this, DoubleCamera.class);
        //MainActivity.this.startActivity(cameraIntent);
        Log.i(TAG, " ACTIVATE ROAD");
        Toast.makeText(getApplicationContext(), "activating road trip", Toast.LENGTH_LONG).show();

/*
        Intent frontCameraIntent = new Intent(MainActivity.this, FrontCameraService.class);
        bindService(frontCameraIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(frontCameraIntent);

        Intent emotionIntent = new Intent(MainActivity.this, EmotionService.class);
        bindService(emotionIntent, serviceConnection, BIND_AUTO_CREATE);
        //MainActivity.this.startService(emotionIntent);

        Intent fatigueIntent = new Intent(MainActivity.this, FatigueService.class);
        bindService(fatigueIntent, serviceConnection, BIND_AUTO_CREATE);
        //MainActivity.this.startService(fatigueIntent);
/ *
        Intent pedestrianIntent = new Intent(MainActivity.this, PedestrianService.class);
        bindService(pedestrianIntent, serviceConnection, BIND_AUTO_CREATE);
        //MainActivity.this.startService(pedestrianIntent);
 * /
        Intent rPPGIntent = new Intent(MainActivity.this, rPPGService.class);
        bindService(rPPGIntent, serviceConnection, BIND_AUTO_CREATE);
        //MainActivity.this.startService(rPPGIntent);
*/
        Intent crashServiceIntent = new Intent(MainActivity.this, CrashService.class);
        bindService(crashServiceIntent, serviceConnection, BIND_AUTO_CREATE );
        MainActivity.this.startService(crashServiceIntent);


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

    public void hizGoster(View view) {
    }

    public void startKenBurnsView(){
        int colorCodeDark = Color.parseColor("#FF9800");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colorCodeDark);
        setContentView(R.layout.nav_activity_main);

        kbv = findViewById(R.id.kbv);

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
                        }
                        else if (item.getItemId() == R.id.nav_gallery) {
                            //Toast.makeText(MainActivity.this,  "home", Toast.LENGTH_SHORT).show();
                            Intent addPetIntent = new Intent(MainActivity.this, SettingsActivity.class);
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
        FloatingActionButton btnAddPet = findViewById(R.id.addPhoto);
        btnAddPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPetIntent = new Intent(MainActivity.this, SendFaceData.class);
                MainActivity.this.startActivity(addPetIntent);
            }
        });
    }
}



