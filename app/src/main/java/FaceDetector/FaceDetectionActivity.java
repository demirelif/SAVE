package FaceDetector;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;

import com.example.saveandroid.R;

import java.lang.ref.WeakReference;

public class FaceDetectionActivity extends AppCompatActivity {
    MediaPlayer player;
    public static WeakReference<FaceDetectionActivity> weakActivity;
    public static final int REQUEST_CODE_PERMISSION = 101;
    //public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    public static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private TextureView tv;
    private ImageView iv;
    private TextView smilingProbability;
    private static final String TAG = "FaceDetectionActivity";

    public static CameraX.LensFacing lens = CameraX.LensFacing.FRONT;

    public static FaceDetectionActivity getInstanceActivity() {
        return weakActivity.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        tv = findViewById(R.id.face_texture_view);
        iv = findViewById(R.id.face_image_view);
        smilingProbability = findViewById(R.id.smilingProbability);

        weakActivity = new WeakReference<>(FaceDetectionActivity.this);

        if (allPermissionsGranted()) {
            tv.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSION);
        }
    }

    @SuppressLint("RestrictedApi")
    private void startCamera() {
        initCamera();
        ImageButton ibSwitch = findViewById(R.id.btn_switch_face);
        ibSwitch.setOnClickListener(v -> {
            if (lens == CameraX.LensFacing.FRONT)
                lens = CameraX.LensFacing.BACK;
            else
                lens = CameraX.LensFacing.FRONT;
            try {
                Log.i(TAG, "" + lens);
                CameraX.getCameraWithLensFacing(lens);
                initCamera();
            } catch (CameraInfoUnavailableException e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    private void initCamera() {
        CameraX.unbindAll();
        PreviewConfig pc = new PreviewConfig
                .Builder()
                .setTargetResolution(new Size(tv.getWidth(), tv.getHeight()))
                .setLensFacing(lens)
                .build();

        Preview preview = new Preview(pc);

        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup vg = (ViewGroup) tv.getParent();
            vg.removeView(tv);
            vg.addView(tv, 0);
            tv.setSurfaceTexture(output.getSurfaceTexture());
        });

        ImageAnalysisConfig iac = new ImageAnalysisConfig
                .Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetResolution(new Size(tv.getWidth(), tv.getHeight()))
                .setLensFacing(lens)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(iac);
        imageAnalysis.setAnalyzer(Runnable::run,
                new MLKitFacesAnalyzer(tv, iv, lens, smilingProbability));
        CameraX.bindToLifecycle(this, preview, imageAnalysis);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (allPermissionsGranted()) {
                tv.post(this::startCamera);
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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

}