package com.example.saveandroid;

import android.app.Service;
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
import CameraApp.EmotionService;
import CameraApp.FatigueService;
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
    private static final String TAG = "MAIN ACTIVITY";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;
    private KenBurnsView kbv;
    private boolean moving = true;
    static Bitmap data;
    boolean cameraBounded;
    boolean emotionBounded;
    boolean fatigueBounded;
    boolean pedestrianBounded;
    boolean rPPGBounded;
    CameraService cameraServer;
    EmotionService emotionServer;
    FatigueService fatigueServer;
    PedestrianService pedestrianServer;
    rPPGService rPPGServer;


    public static double gazeAngle = 0;
    public static double headPose = 0;
    //String url = "http://" + "192.168.1.20" + "/" + "predict";
    private java.net.URL URL;
   // private String url = "http://" + "10.0.0.2" + "/" + "predict"; // try 10.0.0.2
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Response response;
    File file;
    MediaType JSON;

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

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, " onServicedDisconnected");
            // set bounded booleans to false
            cameraBounded = false;
            emotionBounded = false;
            fatigueBounded = false;
            pedestrianBounded = false;
            rPPGBounded = false;
            // disconnect servers
            cameraServer = null;
            emotionServer = null;
            fatigueServer = null;
            pedestrianServer = null;
            rPPGServer = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        startKenBurnsView(); // start special ken burns view

        //Intent intent = new Intent(MainActivity.this, CameraService.class);
        //bindService(intent, serviceConnection, BIND_AUTO_CREATE);


        // server connection
        try {
            postRequest("deneme");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void activateRoadTrip(View view){
        //Intent faceIntent = new Intent(MainActivity.this, FaceDetectionActivity.class);
        // MainActivity.this.startActivity(faceIntent);
        //Intent cameraIntent = new Intent(MainActivity.this, DoubleCamera.class);
        //MainActivity.this.startActivity(cameraIntent);
        Log.i(TAG, " ACTIVATE ROAD");
        Toast.makeText(getApplicationContext(), "activating road trip", Toast.LENGTH_LONG).show();

        Intent cameraIntent = new Intent(MainActivity.this, CameraService.class);
        bindService(cameraIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(cameraIntent);

        Intent emotionIntent = new Intent(MainActivity.this, EmotionService.class);
        bindService(emotionIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(emotionIntent);

        Intent fatigueIntent = new Intent(MainActivity.this, FatigueService.class);
        bindService(fatigueIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(fatigueIntent);

        Intent pedestrianIntent = new Intent(MainActivity.this, PedestrianService.class);
        bindService(pedestrianIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(pedestrianIntent);

        Intent rPPGIntent = new Intent(MainActivity.this, rPPGService.class);
        bindService(rPPGIntent, serviceConnection, BIND_AUTO_CREATE);
        MainActivity.this.startService(rPPGIntent);
    }

    // http request
    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
     //   ByteArrayOutputStream stream = new ByteArrayOutputStream();
      //  data.compress(Bitmap.CompressFormat.JPEG, 100, stream);
     //   byte[] byteArray = stream.toByteArray();
     //   data.recycle();

       // MediaType mediaType = MediaType.parse("multipart/form-data; boundary=--------------------------205063402178265581033669");
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "p2.jpeg",
                        RequestBody.create("image",MediaType.parse("image/*jpg")))
                .addFormDataPart("gaze_offset", "-0.018")
                .addFormDataPart("pose_offset", "0.061")
                .build();
       // Response response = client.newCall(requestBody).execute();
       // file = new File(Environment.getExternalStorageDirectory(),"p2.jpeg");
       // mediaType = MediaType.parse("text/plain");
       // mediaType = MediaType.parse("image/*");
       // requestBody = RequestBody.create(file, mediaType);
        return requestBody;
    }

    private void post2(){

    }
    //private void connectServer
    private void postRequest(String message) throws MalformedURLException {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    RequestBody requestBody = buildRequestBody(message);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    String protocol = "HTTP";
                    String host = "10.0.2.2";
                   // String host = "192.168.1.20";

                    //int port = 8002;
                    String endpoint = "/predict";
                    JSON = MediaType.parse("application/json; charset=utf-8");
                    //URL url = new URL(protocol, host, port, endpoint);
                    URL url = new URL(protocol, host, endpoint);
                    Request request = new Request.Builder()
                            .url(url)
                            .method("POST", requestBody)
                            .build();
                    //Response response = client.newCall(request).execute();
                    /*
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(final Call call, final IOException e) {
                            Log.e("hi","calismadi");
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            Log.i("hi","onresponse");
                        }
                    });

                     */
                    try{
                        response = okHttpClient.newCall(request).execute();
                    }
                    catch (IOException e) {
                        Log.e("hi", "calismadi yine");
                    }

                    String s = "?";
                    if ( response!=null)
                        s = response.body().string();
                  //  JSONObject json = new JSONObject(response.body().string());
                  //  String s= json.toString();
                    Log.i("hi",s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }


    protected void onPostExecute(JSONObject getResponse) {
        if (getResponse != null) {
            try {
                String gazeString = getResponse.get("gaze_offset").toString();
                String headString = getResponse.get("pose_offset").toString();
                String confidentString = getResponse.get("is_confident").toString();
                boolean isConfident = confidentString.equals("true") ? true : false;
                System.out.println("gaze: " + gazeAngle + " headPose: " + headPose + " isConfident: " + isConfident);
                if(!isConfident){
                    Toast.makeText(
                            MainActivity.this,
                            "Calibration Failed, Please Try Again.",
                            Toast.LENGTH_LONG)
                            .show();
                }
                else{
                    gazeAngle = Double.parseDouble(gazeString);
                    headPose = Double.parseDouble(headString);
                    Toast.makeText(
                            MainActivity.this,
                            "Calibration Successful, You can use attention tracking tool!",
                            Toast.LENGTH_LONG)
                            .show();
                }

            } catch (Exception e) {
                Log.e("Error :(", "--" + e);
            }
        }

    }

    public JSONObject get(String url) throws IOException {
        try {
            String img = "p2.jpeg";
            postRequest("deneme");
            JSONObject json = new JSONObject(response.body().string());
            Log.i("Network", "json is ready");
            Log.i("Network", json.toString());
            return json;
            //  return new JSONObject(response.body().string());

        } catch (UnknownHostException | UnsupportedEncodingException e) {
            System.out.println("Error: " + e.getLocalizedMessage());
            Log.e("Network", e.getLocalizedMessage());
        } catch (Exception e) {
            System.out.println("Other Error: " + e.getLocalizedMessage());
            Log.e("Network", e.getLocalizedMessage());
        }

        return null;
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



