package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.saveandroid.MainActivity;
import com.example.saveandroid.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


import SpeechRecognition.Speech;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.FrontCameraService.imageBytesRPPG;

import static com.example.saveandroid.MainActivity.openMapRPPG;
public class rPPGService extends Service {
    public IBinder mBinder = new rPPGService.LocalBinder();
    private static final String TAG = "rPPG SERVICE";
    private static byte[] byteArray;
    private static final String SpeedTAG = "Speed rppg";
    private static boolean callRPPGSpeech = false;
    long startTime,endTime,contentLength;
    long initialStart;
    int counter = 0;
    private boolean isInHighPulse;
    private CustomDialogBox customDialogBox = null;
    private static FloatingIcon floatingIcon = null;
    Handler mainHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public rPPGService getServerInstance(){return rPPGService.this;}
    }
    @Override
    public void onCreate() {
        Log.i(TAG, " ON CREATE");
        super.onCreate();
        cleanRPPGServer();
        isInHighPulse = false;
        mainHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " onDestroy...");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " onStartCommand...");

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    consumer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t3.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void consumer() throws InterruptedException {
        while (true){
            Thread.sleep(500);
            //imageFile = fileQueue.take();
            //Log.i(TAG, "Taken image path: " + imageFile.getPath() + "; Queue size is: " + fileQueue.size());
            //postImageToServer(imageFile);
            byteArray = imageBytesRPPG.take();

            Log.i(TAG, "Consumed byte array length: " + byteArray.length + "; RPPG Queue size is: " + imageBytesRPPG.size());
            if ( counter == 0 )
                startTime = System.currentTimeMillis(); //Hold StartTime
            counter++;
            bullShitPPG();
            postImageToServer(byteArray);
            //bullShitPPG();
        }
    }

    public void bullShitPPG() throws InterruptedException{
        if(callRPPGSpeech){
            Log.i(TAG, "CALL RPPG SPEECH TRUE");
            mainHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.getInstanceActivity().startSpeech("rPPG");
                        }
                    }
            );

        }




    }
    private void cleanRPPGServer(){
        String postUrl2 = "http://" + "192.168.1.102" + ":" + 8000 + "/clean"; // UTKU IP
        String postUrl3 = "http://" + "10.0.2.2" + ":" + 8000 + "/clean";
        String postUrl4 = "http://" + "172.20.10.2" + ":" + 8000 + "/clean"; // UTKU HOTSPOT IP
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //multipartBodyBuilder.addFormDataPart("image", "clean_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        multipartBodyBuilder.addFormDataPart("clean", "selam"); // tamamen random bir sey verdim
        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(postUrl4, postBodyImage);
    }

    private void postImageToServer(byte[] byteArray){
        String postUrl2 = "http://" + "192.168.1.102" + ":" + 8000 + "/rppg"; // UTKU IP
        String postUrl3 = "http://" + "10.0.2.2" + ":" + 8000 + "/rppg";
        String postUrl4 = "http://" + "172.20.10.2" + ":" + 8000 + "/rppg"; // UTKU HOTSPOT IP

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        //System.out.println("TIMESTAMP MATE :  " + ts);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "front_face_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        multipartBodyBuilder.addFormDataPart("timestamp", ts);
        RequestBody postBodyImage = multipartBodyBuilder.build();
        // post request to emotion server
        //postRequest(postUrl, postBodyImage);
        // post request to rppg server
        postRequest(postUrl4, postBodyImage);
    }


    private void postRequest(String postUrl, RequestBody postBody) {

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
                final String[] responzee = {""};
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            responzee[0] = response.body().string();
                            Log.i(TAG, "Server's Response ---> " + responzee[0]);
                            if (!(responzee[0].equals("Calculating...") || responzee[0].equals("cleaned successfully"))){

                                String str = responzee[0];
                                String mid_str = str.replaceAll("[^0-9]", "");
                                System.out.println("mid_str " + mid_str);
                                int pulse_rate = 0;
                                Speech.readText(responzee[0]);
                                if(!mid_str.equals("")){
                                    try{
                                        pulse_rate = Integer.parseInt(mid_str);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                if(pulse_rate > 700){
                                    Speech.readText("Your pulse rate seems above normal, it is " + pulse_rate/10.0 + ", what do you want to do?");
                                    Thread.sleep(1600);
                                    String lat = "39.895166";
                                    String lon = "32.806005";
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.getInstanceActivity().startSpeech("rPPG");
                                        }
                                    });
                                    //MainActivity.getInstanceActivity().openGoogleMaps("hospital");
                                    //MainActivity.getInstanceActivity().sendSMS("+905077907940", "Check Utku from RPPG!! On coordinations lat: " + lat + ", lon: " + lon);
                                    Log.i(TAG, "DISPLAY KISMINDAN CIKTIM");
                                }
                            }
                            else{
                                Log.i(TAG, "rPPG RESPONSE ---> " + responzee[0] );
                                if ( counter >= 50  ) {
                                    endTime = System.currentTimeMillis();  //Hold EndTime
                                    Log.d(SpeedTAG, (endTime - startTime) + " ms");
                                    counter = 0;
                                }
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

}
