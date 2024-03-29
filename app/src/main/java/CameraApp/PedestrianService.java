package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

import SpeechRecognition.Speech;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.BackCameraService.imageBytes;

public class PedestrianService extends Service {
    public IBinder mBinder = new PedestrianService.LocalBinder();
    private static final String TAG = "PEDESTRIAN SERVICE";
    private static byte[] byteArray;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public PedestrianService getServerInstance(){return PedestrianService.this;}
    }
    @Override
    public void onCreate() {
        Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate();
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
            byteArray = imageBytes.take();
            Log.i(TAG, "Consumed byte array length: " + byteArray.length + "; Pedestrian Queue size is: " + imageBytes.size());
            postImageToServer(byteArray);
        }
    }

    private void postImageToServer(byte[] byteArray){
        String postUrl4 = "http://" + "192.168.1.102" + ":" + 7000 + "/pedestrian"; // UTKU IP
        //String postUrl3 = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion"; // ELIF IP

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        System.out.println("TIMESTAMP:  " + ts);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "back_camera_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        multipartBodyBuilder.addFormDataPart("timestamp", ts);
        RequestBody postBodyImage = multipartBodyBuilder.build();
        // post request to emotion server
        //postRequest(postUrl, postBodyImage);
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
                final String[] responzee = {""};
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.responseText);
                        try {
                            responzee[0] = response.body().string();
                            //Toast.makeText(getApplicationContext(), "Server's Response\n" + response.body().string(), Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Server's Response ---> " + responzee[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
