package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import SpeechRecognition.Speech;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.annotation.Nullable;
import androidx.core.graphics.BitmapCompat;

import com.example.saveandroid.MainActivity;
import com.example.saveandroid.R;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.FrontCameraService.imageBitmapFatigue;
import static com.example.saveandroid.MainActivity.openMapFatigue;

public class FatigueService extends Service {
    public IBinder mBinder = new LocalBinder();
    private static final String TAG = "FATIGUE SERVICE";
    private FirebaseVisionFaceDetector faceDetector;
    private FirebaseVisionImage fbImage;
    private static FloatingIcon floatingIcon = null;
    public static double gazeAngle = 0;
    public static double headPose = 0;
    private static double gaze_offset;
    private static double pose_offset;

    private java.net.URL URL;
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Response response;
    private static final String SpeedTAG = "Speed Fatigue";
    long startTime,endTime,contentLength;
    File file;
    MediaType JSON;
    int picNo =0;
    private static Image image;
    String file_name;
    private static byte[] byteArray;
    private static Bitmap fatigueBitmap;
    private float smileProb;
    private float leftEyeOpenProb;
    private float rightEyeOpenProb;
    private int fatigue_frame_counter;
    private int blink_frame_counter;
    private int blink_counter;
    private int yawn_frame_counter;
    private int alarm_counter;
    private final int FATIGUE_THRESHOLD = 3;
    private final int BLINK_THRESHOLD = 3;
    private final int YAWN_THRESHOLD = 20;
    private CustomDialogBox customDialogBox = null;
    private boolean isInFatigue = false;

    boolean enteredThePlace;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
       public FatigueService getServerInstance() { return FatigueService.this; }
    }

    @Override
    public void onCreate() {
        //Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        Log.i(TAG, " ON CREATE");
        super.onCreate();
        gaze_offset = 0.0;
        pose_offset = 0.0;
        smileProb = 0;
        leftEyeOpenProb = 0;
        rightEyeOpenProb = 0;
        fatigue_frame_counter = 0;
        blink_frame_counter = 0;
        blink_counter = 0;
        yawn_frame_counter = 0;
        alarm_counter = 0;
        enteredThePlace = false;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " onDestroy...");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " onStartCommand");
        Thread fatigue = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    consumer();
                } catch (InterruptedException | MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        fatigue.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void consumer() throws InterruptedException, IOException {
        while (true){
            Thread.sleep(500);
            fatigueBitmap = imageBitmapFatigue.take();
            int bitmapByteCount = BitmapCompat.getAllocationByteCount(fatigueBitmap);
            Log.i(TAG, "Fatigue BITMAP SIZE: " + bitmapByteCount + "  Fatigue Queue size is: " + imageBitmapFatigue.size());
            startTime = System.currentTimeMillis(); //Hold StartTime
            analyze(fatigueBitmap);
            endTime = System.currentTimeMillis();  //Hold EndTime
            Log.d(SpeedTAG, (endTime - startTime) + " ms");
        }
    }

    public void analyze(Bitmap fatigueBitmap) {
        if (fatigueBitmap == null) {
            return;
        }
        fbImage = FirebaseVisionImage.fromBitmap(fatigueBitmap);
        initDetector();
        detectFaces();
    }

    private void initDetector() {
        FirebaseVisionFaceDetectorOptions detectorOptions = new FirebaseVisionFaceDetectorOptions
                .Builder()
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();
        faceDetector = FirebaseVision
                .getInstance()
                .getVisionFaceDetector(detectorOptions);
    }

    private void detectFaces() {
        faceDetector
                .detectInImage(fbImage)
                .addOnSuccessListener(firebaseVisionFaces -> {
                    if (!firebaseVisionFaces.isEmpty()) {
                        try {
                            getInfoFromFaces(firebaseVisionFaces);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("FIREBASE VISION FACES EMPTY");
                    }
                }).addOnFailureListener(e -> Log.i(TAG, e.toString()));
    }

    private void getInfoFromFaces(List<FirebaseVisionFace> faces) throws InterruptedException {
        StringBuilder result = new StringBuilder();
        for (FirebaseVisionFace face : faces) {
            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and nose available):
            smileProb = face.getSmilingProbability();
            leftEyeOpenProb = face.getLeftEyeOpenProbability();
            rightEyeOpenProb = face.getRightEyeOpenProbability();
            DecimalFormat numberFormat = new DecimalFormat("#0.00");
            result.append("Smile: ");
            result.append( numberFormat.format(smileProb) );
            result.append("\nLeft Eye Open: ");
            result.append(numberFormat.format(leftEyeOpenProb) );
            result.append("\nRight Eye Open: ");
            result.append(numberFormat.format(rightEyeOpenProb));
            result.append("\n");

            /*	30 frame for fatigue
            	3 frame for blink
            	20 frame for yawn */

            if(leftEyeOpenProb <= 0.5 && rightEyeOpenProb <= 0.5){

                fatigue_frame_counter++;
                blink_frame_counter++;

                if(fatigue_frame_counter >= FATIGUE_THRESHOLD && !enteredThePlace){
                    enteredThePlace = true;
                    result.append("FATIGUE ALERT");
                    Log.i(TAG, " FATIGUE ALERT");
                    MainActivity.getInstanceActivity().playAlarm();
                    alarm_counter++;
                    if(alarm_counter > 3){
                        fatigue_frame_counter = 0;
                        alarm_counter = 0;

                        Speech.readText("You show fatigue symptoms. Consider having a stopover ?");
                        Log.i(TAG, "YOU SHOW FATIGUE SYMPTOMS. DO YOU WANT US TO SAVE THE SITUATION");

                        MainActivity.getInstanceActivity().startSpeech();

                        Thread.sleep(2000);
                        // semaphore falan lazım ?

                        String userResponse = MainActivity.getInstanceActivity().getSpeechString();
                        Log.i(TAG, "USER RESPONSE IN FATIGUE " + userResponse);

                        if (userResponse.equals("Evet")){
                            Log.i(TAG, "IF ICINDE");
                            Speech.readText("YEEEEEEEEEEEEE");
                            if(!isInFatigue){
                                isInFatigue = true;
                                DisplayIcon();
                                DisplayDialog();
                            }
                        }
                        //DisplayIcon();
                        //DisplayDialog();


                        Log.i(TAG, "DISPLAY KISMINDAN CIKTIM");
                    }
                    Thread.sleep(500);
                    enteredThePlace = false;
                }

                if(blink_frame_counter >= BLINK_THRESHOLD){
                    blink_counter++;
                    System.out.println("Number of Blinks =" + blink_counter);
                }
            }
            else{
                fatigue_frame_counter = 0;
                blink_frame_counter = 0;
            }
            System.out.println("\n" + result);
        }

    }

    private void DisplayIcon() {
        floatingIcon = new FloatingIcon(this, new ICustomBubbleListener() {
            @Override
            public void onFloatingIconClicked(View v) {
                showCustomDlg(v);
            }
        });
    }

    private void DisplayDialog() {
        if (customDialogBox == null)
            customDialogBox = new CustomDialogBox(this, new ICustomDialogListener() {
                @Override
                public void onClick(View view) {
                    showCustomDlg(view);
                }

                @Override
                public void dlgTimeOut() {
                    checkTimeOut();
                }
            }, DialogType.Fatigue, 10);
    }

    private void SetStateToSafeMode() {
        if (isInFatigue) {
            isInFatigue = false;
            if (floatingIcon != null)
                floatingIcon.Remove();
            if (customDialogBox != null){
                customDialogBox.Remove();
                customDialogBox = null;
            }
        }
    }

    public void showCustomDlg(View view) {
        if (view.getId() == R.id.csbubbleimg)
            DisplayDialog();
        else if(view.getId()== R.id.btnYes){
            SetStateToSafeMode();
            MainActivity.getInstanceActivity().openGoogleMaps("station");
        }
        else if(view.getId() == R.id.btnNo){
            SetStateToSafeMode();
        }
        else
            SetStateToSafeMode();
    }
    public void checkTimeOut() {
        if (customDialogBox != null) {
            customDialogBox.Remove();
            customDialogBox = null;
        }
    }
}
