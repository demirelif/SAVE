package com.example.saveandroid;

import android.graphics.Color;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;




import android.view.MenuItem;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.flaviofaria.kenburnsview.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private KenBurnsView kbv;
    private boolean moving = true;

    private String url = "http://" + "10.0.0.2" + ":" + "8000"; // try 10.0.0.2
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //IdentityManager.getDefaultIdentityManager().signOut();

        postRequest("your message here", url);

        super.onCreate(savedInstanceState);
        startKenBurnsView(); // start special ken burns view

    }


    public void activateFaceTracking(View view){
        //Intent faceIntent = new Intent(MainActivity.this, FaceDetectionActivity.class);
        // MainActivity.this.startActivity(faceIntent);

    }

    // http request

    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        mediaType = MediaType.parse("text/plain");
        requestBody = RequestBody.create(postBodyString, mediaType);
        return requestBody;
    }



    private void postRequest(String message, String URL) {
        RequestBody requestBody = buildRequestBody(message);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request
                .Builder()
                .post(requestBody)
                .url(URL)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();
                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(MainActivity.this, response.body().string(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
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

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}



