package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import androidx.annotation.Nullable;

import com.example.saveandroid.MainActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static CameraApp.FrontCameraService.queue;

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
                }
            }
        });
        fatigue.start();
        return super.onStartCommand(intent, flags, startId);
    }

    //private void connectServer
    private void postRequest(String message) throws MalformedURLException {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    RequestBody requestBody = buildRequestBody(message);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    String protocol = "HTTP";
                    String host = "10.0.2.2";
                    // String host = "192.168.1.20";

                    //int port = 8002;
                    String endpoint = "/predict";
                    JSON = MediaType.parse("application/json; charset=utf-8");
                    //URL url = new URL(protocol, host, port, endpoint);
                    java.net.URL url = new URL(protocol, host, endpoint);
                    Request request = new Request.Builder()
                            .url(url)
                            .method("POST", requestBody)
                            .build();
                    //Response response = client.newCall(request).execute();
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
                        Log.e(TAG, "calismadi yine");
                    }

                    String s = "?";
                    if ( response!=null)
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
    private RequestBody buildRequestBody(String msg) throws JSONException {
        postBodyString = msg;
        final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
        //   ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //  data.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        //   byte[] byteArray = stream.toByteArray();
        //   data.recycle();
//        JSONObject j = new JSONObject(msg);
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", msg)
                .addFormDataPart("gaze_offset", "-0.018")
                .addFormDataPart("pose_offset", "0.061")
                .build();

        // MediaType mediaType = MediaType.parse("multipart/form-data; boundary=--------------------------205063402178265581033669");
       /*
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "p2.jpeg",
                        RequestBody.create("image",MediaType.parse("image/*jpg")))
                .addFormDataPart("gaze_offset", "-0.018")
                .addFormDataPart("pose_offset", "0.061")
                .build();

        */


        // Response response = client.newCall(requestBody).execute();
        // file = new File(Environment.getExternalStorageDirectory(),"p2.jpeg");
        // mediaType = MediaType.parse("text/plain");
        // mediaType = MediaType.parse("image/*");
        // requestBody = RequestBody.create(file, mediaType);
        return requestBody;
    }

    private void post2(){

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

    private void consumer() throws InterruptedException, MalformedURLException {
        //Random random = new Random();
        while (true){
            //Thread.sleep(500);
            byte[] emotionPhoto = queue.take();
            if ( emotionPhoto == null ) { Log.e(TAG, "BYTE ARRAY IS EMPTY"); }
            Log.i(TAG, "Taken value: " + "emotion photo" + "; Queue size is: " + queue.size());

            Bitmap bmp= BitmapFactory.decodeByteArray(emotionPhoto,0,emotionPhoto.length);
            String j = getStringFromBitmap(bmp);
            //JSONObject obj = new JSONObject(j);
           // JSONObject obj = new JSONObject();
           // obj.put(emotionPhoto);
           // jsonObj.put(byte[]);
            postRequest(j);
        }
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        if ( bitmapPicture == null ) { return null; }
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public JSONObject get(String url) throws IOException {
        try {
            String img = "p2.jpeg";
            postRequest("deneme");
            JSONObject json = new JSONObject(response.body().string());
            Log.i("Network", "json is ready");
            Log.i("Network", json.toString());
            return json;
            //  return new JSONObject(response.body().string());

        } catch (UnknownHostException | UnsupportedEncodingException e) {
            System.out.println("Error: " + e.getLocalizedMessage());
            Log.e("Network", e.getLocalizedMessage());
        } catch (Exception e) {
            System.out.println("Other Error: " + e.getLocalizedMessage());
            Log.e("Network", e.getLocalizedMessage());
        }

        return null;
    }
}
