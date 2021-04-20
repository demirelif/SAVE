package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//import static CameraApp.CameraService.queue;
import static CameraApp.FrontCameraService.imageQueue;

public class EmotionService extends Service {
    public IBinder mBinder = new EmotionService.LocalBinder();
    private static final String TAG = "EMOTION SERVICE";
    private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion";
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
    private static int port;
    //String endpoint = "/predict_emotion";
    //String endpoint = "/lol";
    private static String endpoint;
    private Image image;
    int picNo = 1;

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

       try {
            postRequest("");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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

    //private void connectServer
    private void postRequest(String message) throws MalformedURLException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    RequestBody requestBody = buildRequestBody(message);
                    okHttpClient = new OkHttpClient();
                    JSON = MediaType.parse("application/json; charset=utf-8");
                   // URL url = new URL(protocol, host,endpoint);
                    java.net.URL url = new URL(protocol, host, port, endpoint);
                    Request request = new Request.Builder()
                            .url(url)
                            .method("POST", requestBody)
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(final Call call, final IOException e) {
                            Log.e("hi","emotion fail");
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            Log.i("hi","emotion responsed");
                        }
                    });


                    try{
                        response = okHttpClient.newCall(request).execute();
                    }
                    catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }

                    String s = "Emotion Failed";
                    if ( response!= null)
                        s = response.body().string();
                    Log.i(TAG,s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    // http request
    private static RequestBody buildRequestBodyForMultiply(String msg) {
        postBodyString = msg;
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "p2.jpeg", RequestBody.create("image",MediaType.parse("image/*jpg")))
                .build();
        return requestBody;
    }

    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        if ( image == null ) return requestBody;

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        String fname = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pic" + picNo + ".jpg";
        fname = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pic1_45.jpg";
        Log.d(TAG, "Saving:" + fname);
        /*
        //File file = new File(fname);
        //byte[] bytes = new byte[buffer.remaining()];
        //buffer.get(bytes);
        try {
            //save(bytes, file); // save image here
            OutputStream output = null;
            output = new FileOutputStream(file);
            output.write(bytes);


        } catch (IOException e) {
            e.printStackTrace();
        }
        //image.close();
         */
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", fname,
                        RequestBody.create("image",MediaType.parse("image/*jpg")))
                .build();

        return requestBody;
    }

    private void consumer() throws InterruptedException, MalformedURLException {
        //Random random = new Random();
        while (true){
            //Thread.sleep(500);
            image = imageQueue.take();
            Log.i(TAG, "Taken value: " + "emotion photo" + "; Queue size is: " + imageQueue.size());
            try {
                postRequest("emotion photo");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

}
