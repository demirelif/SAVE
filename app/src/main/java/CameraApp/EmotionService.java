package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.CameraService.queue;

public class EmotionService extends Service {
    public IBinder mBinder = new EmotionService.LocalBinder();
    private static final String TAG = "EMOTION SERVICE";
    //private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion";
    //String postUrl = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion";
    private static java.net.URL URL;
    private static String postBodyString;
    private static MediaType mediaType;
    private static RequestBody requestBody;
    private static Response response;
    private static File file;
    private static MediaType JSON;
    private static OkHttpClient okHttpClient;
    private static String protocol;
    private static String host;
    // String host = "192.168.1.20";
    // String host = "192.168.1.103";
    private static int port;
    //String endpoint = "/predict_emotion";
    //String endpoint = "/lol";
    private static String endpoint;

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
        Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate();

/*        try {
            postRequest("deneme");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t2.start();
/*        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        return super.onStartCommand(intent, flags, startId);
    }

    //private void connectServer
    private static void postRequest(String message) throws MalformedURLException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    RequestBody requestBody = buildRequestBodyForMultiply(message);
                    okHttpClient = new OkHttpClient();
                    //RequestBody requestBody = buildRequestBodyForMultiply(message);
                    JSON = MediaType.parse("application/json; charset=utf-8");
                    URL url = new URL(protocol, host,endpoint);
                    //java.net.URL url = new URL(protocol, host, port, endpoint);
                    Request request = new Request.Builder()
                            .url(url)
                            .method("POST", requestBody)
                            .build();
                    try{
                        response = okHttpClient.newCall(request).execute();
                    }
                    catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    String s = "OLMADI";
                    if ( response!= null)
                        s = response.body().string();
                    //  JSONObject json = new JSONObject(response.body().string());
                    //  String s= json.toString();
                    Log.i(TAG,s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    // http request
    private static RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "p2.jpeg", RequestBody.create("image",MediaType.parse("image/*jpg")))
                .build();
        return requestBody;
    }

    private static RequestBody buildRequestBodyForMultiply(String msg) {
        postBodyString = msg;
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("sent", postBodyString)
                .build();
        return requestBody;
    }

    private static void consumer() throws InterruptedException {
        //Random random = new Random();
        while (true){
            //Thread.sleep(500);
            Integer value = queue.take();
            Log.i(TAG, "Taken value: " + value + "; Queue size is: " + queue.size());
/*            try {
                postRequest(value.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
/*            if(random.nextInt(10) == 0){
                Integer value = queue.take();
                System.out.println("Taken value: " + value + "; Queue size is: " + queue.size());
            }*/
        }
    }

}
