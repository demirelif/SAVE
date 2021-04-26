package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import androidx.annotation.Nullable;

import com.example.saveandroid.MainActivity;

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

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.FrontCameraService.fileQueue;
import static CameraApp.FrontCameraService.imageBytesFatigue;

public class FatigueService extends Service {
    public IBinder mBinder = new LocalBinder();
    private static final String TAG = "FATIGUE SERVICE";

    public static double gazeAngle = 0;
    public static double headPose = 0;
    private java.net.URL URL;
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Response response;
    File file;
    MediaType JSON;
    int picNo =0;
    private static Image image;
    String file_name;
    private static byte[] byteArray;

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
        Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        super.onCreate();
        // server connection
/*
        try {
            postRequest("deneme");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

 */
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
            //imageFile = fileQueue.take();
            //Log.i(TAG, "Taken image path: " + imageFile.getPath() + "; Queue size is: " + fileQueue.size());
            //postImageToServer(imageFile);
            byteArray = imageBytesFatigue.take();
            Log.i(TAG, "Consumed byte array length: " + byteArray.length + "; Fatigue Queue size is: " + imageBytesFatigue.size());
            postImageToServer(byteArray);
        }
        //Random random = new Random();
        /*
        while (true){
            Thread.sleep(500);
            //file_name = queue.take();
            //Log.i(TAG, "Taken value: " + file_name + "; Queue size is: " + queue.size());

           // Bitmap bmp= BitmapFactory.decodeByteArray(emotionPhoto,0,emotionPhoto.length);
           // String j = getStringFromBitmap(bmp);

            //JSONObject obj = new JSONObject(j);
           // JSONObject obj = new JSONObject();
           // obj.put(emotionPhoto);
           // jsonObj.put(byte[]);
        }*/
    }
    private void postImageToServer(byte[] byteArray) throws IOException {
        String url = "http://" + "10.0.2.2" + "/predict"; // ELIF IP
        OkHttpClient okHttpClient = new OkHttpClient();
        String protocol = "HTTP";
       // String host = "localhost";
        String host = "10.0.2.2";
        String endpoint = "/predict"; // port 8002de degil?
        //JSON = MediaType.parse("application/json; charset=utf-8");
        //URL url = new URL(protocol, host, port, endpoint);
        java.net.URL postUrl = new URL(protocol, host, endpoint);



        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "fatigue_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        multipartBodyBuilder.addFormDataPart("gaze_offset", "-0.018");
        multipartBodyBuilder.addFormDataPart("pose_offset", "0.061");

        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(url, postBodyImage);
    }

    //private void connectServer

    void postRequest(java.net.URL postUrl, RequestBody postBody) {
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
                Log.d("FAIL FATIGUE", e.getMessage());
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
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.responseText);
                        try {
                            //Toast.makeText(getApplicationContext(), "Server's Response\n" + response.body().string(), Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Server's Response\n" + response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    void postRequest(String postUrl, RequestBody postBody) throws IOException {
        OkHttpClient client = new OkHttpClient();
       /*
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        */
        Request request = new Request.Builder()
                .url(postUrl)
                .method("POST", postBody)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        }  catch (IOException e) {
            Log.e(TAG, "FATIGUE FAIL");
        }

        String s = "Empty Response";
        if ( response != null )
            s = response.body().string();
        Log.i(TAG,s);

        /*
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL FATIGUE", e.getMessage());
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
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.responseText);
                        try {
                            //Toast.makeText(getApplicationContext(), "Server's Response\n" + response.body().string(), Toast.LENGTH_LONG).show();
                            Log.i(TAG, "Server's Response\n" + response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

         */
    }

    private void oldpostRequest(String url, RequestBody message) throws MalformedURLException {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
           //         RequestBody requestBody = buildRequestBody(message);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    String protocol = "HTTP";
                   // String host = "10.0.2.2";
                    String host = "localhost";
                    String endpoint = "/predict"; // port 8002de degil?
                 //   JSON = MediaType.parse("application/json; charset=utf-8");
                    //URL url = new URL(protocol, host, port, endpoint);
                    java.net.URL url = new URL(protocol, host, endpoint);
                    Request request = new Request.Builder()
                            .url(url)
                            .method("POST", requestBody)
                            .build();
                   // Response response = client.newCall(request).execute();
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
                        Log.e(TAG, "FATIGUE FAIL");
                    }
                    String s = "? - empty response";
                    if ( response!=null)
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
    private RequestBody buildRequestBody(String msg) throws JSONException {
        postBodyString = msg;
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", file_name,
                        RequestBody.create("image",MediaType.parse("image/*jpg")))
                .addFormDataPart("gaze_offset", "-0.018")
                .addFormDataPart("pose_offset", "0.061")
                .build();
        return requestBody;
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
                            getApplicationContext(),
                            "Calibration Failed, Please Try Again.",
                            Toast.LENGTH_LONG)
                            .show();
                }
                else{
                    gazeAngle = Double.parseDouble(gazeString);
                    headPose = Double.parseDouble(headString);
                    Toast.makeText(
                            getApplicationContext(),
                            "Calibration Successful, You can use attention tracking tool!",
                            Toast.LENGTH_LONG)
                            .show();
                }

            } catch (Exception e) {
                Log.e("Error :(", "--" + e);
            }
        }

    }
}
