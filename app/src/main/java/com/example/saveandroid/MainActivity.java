package com.example.saveandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import FaceDetector.FaceDetectionActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //IdentityManager.getDefaultIdentityManager().signOut();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btnAddPet = findViewById(R.id.addPhoto);
        btnAddPet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent addPetIntent = new Intent(MainActivity.this, SendFaceData.class);
                MainActivity.this.startActivity(addPetIntent);

            }
        });
    }

    public void activateFaceTracking(View view){
        Intent faceIntent = new Intent(MainActivity.this, FaceDetectionActivity.class);
        MainActivity.this.startActivity(faceIntent);
    }
}



