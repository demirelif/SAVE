package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.saveandroid.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;

import FaceDetector.FaceDetectionActivity;
import SpeechRecognition.Speech;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.HashMap;

import static CameraApp.FrontCameraService.imageBytesEmotion;

public class EmotionService extends Service {
    public IBinder mBinder = new EmotionService.LocalBinder();
    private static final String TAG = "EMOTION SERVICE";
    private static final String SpeedTAG = "Speed Emotion";
    long startTime,endTime,contentLength;
    private static java.net.URL URL;
    private static String postBodyString;
    private static MediaType mediaType;
    private static RequestBody requestBody;
    private static Response response;
    private static File file;
    private static MediaType JSON;
    private static OkHttpClient okHttpClient;
    //private static String protocol;
    private static String host;
    private static int port;
    //String endpoint = "/predict_emotion";
    //String endpoint = "/lol";
    private static String endpoint;
    private Image image;
    int picNo = 1;
    private static String file_name = "";
    private static File imageFile;
    private static File oldImageFile;
    private static byte[] byteArray;
    private static int sadCounter;
    private static int happyCounter;
    private static int angryCounter;
    private static int surprisedCounter;
    private static int neutralCounter;
    private static int fearCounter;
    private static int disgustCounter;

    private static String lastPlayedObservedEmotion;

    public static boolean playHappyPlaylist;
    public static boolean playEnergeticPlaylist;
    public static boolean playCalmPlaylist;

   // HashMap<String, Integer> moods = new HashMap<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public EmotionService getServerInstance(){return EmotionService.this;}
    }
    @Override
    public void onCreate() {
        //Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        Log.i(TAG, " ON CREATE");
        super.onCreate();
        sadCounter = 0;
        happyCounter = 0;
        angryCounter = 0;
        surprisedCounter = 0;
        neutralCounter = 0;
        fearCounter = 0;
        disgustCounter = 0;
        playHappyPlaylist = false;
        playCalmPlaylist = false;
        playEnergeticPlaylist = false;
        lastPlayedObservedEmotion = "";
        //MainActivity.getInstanceActivity().makeCall("");
        //fillMap();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " onDestroy...");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " onStartCommand...");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    consumer();
                } catch (InterruptedException | MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        t2.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void consumer() throws InterruptedException, MalformedURLException {
        while (true){
            Thread.sleep(500);
            byteArray = imageBytesEmotion.take();
            Log.i(TAG, "Consumed byte array length: " + byteArray.length + "; Emotion Queue size is: " + imageBytesEmotion.size());
            startTime = System.currentTimeMillis(); //Hold StartTime
            postImageToServer(byteArray);
            endTime = System.currentTimeMillis();  //Hold EndTime
            Log.d(SpeedTAG, (endTime - startTime) + " ms");
        }
    }

    private void postImageToServer(byte[] byteArray){
        //MainActivity.getInstanceActivity().makeCall("90-50-78-65-2663");
        String postUrl = "http://" + "192.168.1.102" + ":" + 5000 + "/predict_emotion"; // UTKU IP
        String postUrl3 = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion"; // ELIF IP
        String postUrl4 = "http://" + "172.20.10.2" + ":" + 5000 + "/predict_emotion"; // UTKU HOTSPOT IP
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "front_face_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();
        // post request to emotion server
        //postRequest(postUrl3, postBodyImage);
        // post request to rppg server
        postRequest(postUrl4, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.

                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(),"Failed to Connect to Server. Please Try Again.", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Failed to Connect to Server. Please Try Again.");
                    }
                }).start();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                //Timer timer = new Timer();
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()

                new Thread(new Runnable() {
                    String s = "";
                    @Override
                    public void run() {

                        //TextView responseText = findViewById(R.id.responseText);
                        try {
                            //Toast.makeText(getApplicationContext(), "Server's Response\n" + response.body().string(), Toast.LENGTH_LONG).show();
                            //Log.i(TAG, "Server's Response\n" + response.body().string());
                            s = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG, "Server's Response\n" + s);
                        Log.i(TAG, "SAD COUNTER " + sadCounter);
                        Log.i(TAG, "ANGRY COUNTER " + angryCounter);

                        if (s.equals("Sad")){
                            sadCounter++;
                            if(sadCounter > 35 && !lastPlayedObservedEmotion.equals("Sad")){
                                Speech.readText("You seem sad, Do you want to listen some music to cheer up?");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MainActivity.getInstanceActivity().jukeBox("Energetic");
                                sadCounter = 0;
                                lastPlayedObservedEmotion = "Sad";
                                setAllCounterZero();
                            }
                        }else if(s.equals("Angry")){
                            angryCounter++;
                            if(angryCounter > 2 && !lastPlayedObservedEmotion.equals("Angry")){
                                Speech.readText("You seem angry, Do you want some music to relax ?");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MainActivity.getInstanceActivity().jukeBox("Calm");
                                /**
                                 MainActivity.getInstanceActivity().startSpeech();
                                 String userResponse = MainActivity.speechString;
                                 if ( userResponse.equals("yes")) {
                                 MainActivity.getInstanceActivity().jukeBox("Calm");
                                 }*/
                                lastPlayedObservedEmotion = "Angry";
                                setAllCounterZero();
                            }
                        }
                        else if(s.equals("Fear")){
                            fearCounter++;
                            if(fearCounter > 2 && !lastPlayedObservedEmotion.equals("Fear")){
                                Speech.readText("Don't panic, everything is fine");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MainActivity.getInstanceActivity().jukeBox("Fear");
                                lastPlayedObservedEmotion = "Fear";
                                setAllCounterZero();
                            }
                        }
                        else if(s.equals("Happy")){
                            happyCounter++;
                            if(happyCounter > 12 && !lastPlayedObservedEmotion.equals("Happy")){
                                Speech.readText("Happy road trips !!");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                MainActivity.getInstanceActivity().jukeBox("Happy");
                                lastPlayedObservedEmotion = "Happy";
                                setAllCounterZero();
                            }
                        }
                    }
                }).start();
            }
        });
    }

    public void setAllCounterZero(){
        sadCounter = 0;
        happyCounter = 0;
        fearCounter = 0;
        angryCounter = 0;
    }
    /*

    private void resetMap(){
        moods.clear();
        fillMap();
    }

    private void fillMap(){
        moods.put("Sad",0);
        moods.put("Angry",0);
        moods.put("Neutral",0);
        // moods.put()
        // moods.put()
    }

    private void getMaxEmotion(){

    }
     */



}
