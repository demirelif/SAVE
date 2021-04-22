package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
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

import static CameraApp.FrontCameraService.fileQueue;
import static CameraApp.FrontCameraService.imageBytes;

public class EmotionService extends Service {
    public IBinder mBinder = new EmotionService.LocalBinder();
    private static final String TAG = "EMOTION SERVICE";
    private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion";
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
            //imageFile = fileQueue.take();
            //Log.i(TAG, "Taken image path: " + imageFile.getPath() + "; Queue size is: " + fileQueue.size());
            //postImageToServer(imageFile);
            byteArray = imageBytes.take();
            Log.i(TAG, "Consumed byte array length: " + byteArray.length + "; Queue size is: " + imageBytes.size());
            postImageToServer(byteArray);
        }
    }

    private void postImageToServer(byte[] byteArray){
        String postUrl = "http://" + "192.168.1.102" + ":" + 5000 + "/predict_emotion"; // UTKU IP
        String postUrl2 = "http://" + "192.168.1.102" + ":" + 8000 + "/rppg"; // UTKU IP
        //String postUrl3 = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion"; // ELIF IP
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image", "front_face_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();
        // post request to emotion server
        postRequest(postUrl, postBodyImage);
        // post request to rppg server
        //postRequest(postUrl2, postBodyImage);
    }


    private void postImageToServer(@NonNull File imageFile) {
        String filePath = imageFile.getPath();
        //Toast.makeText(getApplicationContext(),"Sending the Files. Please Wait ...", Toast.LENGTH_SHORT).show();

        String postUrl = "http://" + "192.168.1.102" + ":" + 5000 + "/predict_emotion"; // UTKU IP
        String postUrl2 = "http://" + "192.168.1.102" + ":" + 8000 + "/rppg"; // UTKU IP
        //String postUrl3 = "http://" + "10.0.2.2" + ":" + 5000 + "/predict_emotion"; // ELIF IP

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // Read bitmap by file path
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);

            // IMAGE ROTATION PARAMETERS
            //int imageRotation = getImageRotation(imageFile); // EMULATORDE RUNLAYACAKSANIZ BUNU KULLANIN
            int imageRotation = 270; // REAL DEVICE ICIN BUNU
            System.out.println("IMAGE ROTATION " + imageRotation);

            if (imageRotation != 0) // aslında her zaman değil
                bitmap = getBitmapRotatedByDegree(bitmap, imageRotation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            //Toast.makeText(getApplicationContext(),"converting image",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            //Toast.makeText(getApplicationContext(),"Please Make Sure the Selected File is an Image.",Toast.LENGTH_SHORT).show();
            //Log.i(TAG, "Please Make Sure the Selected File is an Image.");
            return;
        }
        byte[] byteArray = stream.toByteArray();
        multipartBodyBuilder.addFormDataPart("image", "front_face_image" + ".jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));

        RequestBody postBodyImage = multipartBodyBuilder.build();
        // post request to emotion server
        postRequest(postUrl, postBodyImage);
        // post request to rppg server
        postRequest(postUrl2, postBodyImage);

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

    private static int getImageRotation(@NonNull File imageFile) {
        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }

    private static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }

    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
