package CameraApp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.saveandroid.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class FrontCameraService extends Service {
    private static final String TAG = "FRONT-CAMERA SERVICE";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;
    public IBinder mBinder = new FrontCameraService.LocalBinder();

    // by elif
    private String frontCameraID;
    private static Image image;
    private String backCameraID;
    private ImageReader frontImageReader;
    private ImageReader backImageReader;
    private CameraDevice frontCameraDevice;
    private CameraDevice backCameraDevice;
    private CameraCaptureSession frontCameraCaptureSession;
    private CameraCaptureSession backCameraCaptureSession;
    static byte[] picture = null;
    private WindowManager windowManager;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Handler frontHandler;
    private HandlerThread frontThread;
    private int frontCounter = 0;
    private int backCounter = 0;
    private static String fname = "";
    CaptureRequest.Builder mPreviewRequestBuilder;
    //public static byte[][] yuvBytes = new byte[3][];
    private static File imageFile;

    public static BlockingQueue<File> fileQueue = new ArrayBlockingQueue<>(10);

    public static BlockingQueue<byte[]> imageBytesRPPG = new ArrayBlockingQueue<>(10);
    //public static BlockingQueue<byte[]> imageBytesFatigue = new ArrayBlockingQueue<>(10);
    public static BlockingQueue<Bitmap> imageBitmapFatigue = new ArrayBlockingQueue<>(10);
    public static BlockingQueue<byte[]> imageBytesEmotion = new ArrayBlockingQueue<>(10);

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        //ORIENTATIONS.append(Surface.ROTATION_0, 90);
       // ORIENTATIONS.append(Surface.ROTATION_90, 0);
        //ORIENTATIONS.append(Surface.ROTATION_180, 270);
        //ORIENTATIONS.append(Surface.ROTATION_270, 180);

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class LocalBinder extends Binder {
        public FrontCameraService getServerInstance(){return FrontCameraService.this;}
    }
    @Override
    public void onCreate() {
        //Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
        Log.i(TAG, " ON CREATE");
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
        Thread frontThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    producer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        setCamera();
        openCamera();

        frontThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    private void setCamera() {
        Log.i("front", "SET CAMERA");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                // getting two cameras here
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    this.frontCameraID = cameraID;
                } else {
                    continue;
                }
            }
            // WARNING - THIS PART SHOULD CHANGE, MUST NOT BE CONSTANT?
            //int pictureWidth = 640;
            //int pictureHeight = 480;
            // utku
            int pictureWidth = 480;
            int pictureHeight = 640;

            //int pictureWidth = 1080;
            //int pictureHeight = 1920;

            // END OF THE WARNING

            frontImageReader = ImageReader.newInstance(pictureWidth, pictureHeight, ImageFormat.JPEG, 10);
            frontImageReader.setOnImageAvailableListener(onImageAvailableListener, frontHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if ( frontCameraID == null ){ Log.e("camera", "cannot find camera"); }
        else
            Log.i("camera", "set camera success");

    }

    private static void producer() throws InterruptedException {
        Log.i("front", "PRODUCER");
        while (true){
            Thread.sleep(500);
            try {
                byte[] byteez = preProcessImage(imageFile);
                if(byteez != null){
                    if(MainActivity.fatigueStarted){
                        imageBitmapFatigue.put(preProcessImageForFatigue(imageFile));
                    }
                    if(MainActivity.emotionStarted){
                        imageBytesEmotion.put(byteez);
                    }
                    if(MainActivity.rPPGStarted){
                        imageBytesRPPG.put(byteez);
                    }
                    Log.i(TAG, "Inserting image bytes: " + byteez.length + "; rPPG Queue size is: " + imageBytesRPPG.size());
                    Log.i(TAG, "Inserting image bytes: " + byteez.length + "; Emotion Queue size is: " + imageBytesEmotion.size());
                    Log.i(TAG, "Inserting image bytes, Fatigue Queue size is: " + imageBitmapFatigue.size());
                }else {
                    Log.i(TAG, "Byteez couldnt make it");
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }
    private static Bitmap preProcessImageForFatigue(@NonNull File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // Read bitmap by file path
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);

            // IMAGE ROTATION PARAMETERS
            //int imageRotation = getImageRotation(imageFile); // EMULATORDE RUNLAYACAKSANIZ BUNU KULLANIN
            int imageRotation = 270; // REAL DEVICE ICIN BUNU

            if (imageRotation != 0) // aslında her zaman değil
                bitmap = getBitmapRotatedByDegree(bitmap, imageRotation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return bitmap;
        }catch (Exception e){
            Log.i(TAG, "FATIGUEEE Please Make Sure the Selected File is an Image.");
            return null;
        }
    }

    private static byte[] preProcessImage(@NonNull File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            // Read bitmap by file path
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), options);

            // IMAGE ROTATION PARAMETERS
            //int imageRotation = getImageRotation(imageFile); // EMULATORDE RUNLAYACAKSANIZ BUNU KULLANIN
            int imageRotation = 270; // REAL DEVICE ICIN BUNU

            if (imageRotation != 0) // aslında her zaman değil
                bitmap = getBitmapRotatedByDegree(bitmap, imageRotation);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            return byteArray;
        }catch (Exception e){
            Log.i(TAG, "Please Make Sure the Selected File is an Image.");
            return null;
        }
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


    private void openCamera() {
        Log.i("front", "OPEN CAMERA");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(frontCameraID, frontCameraStateCallback, frontHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private final CameraDevice.StateCallback frontCameraStateCallback = new CameraDevice.StateCallback() {
       // Log.i("front", "SET CAMERA");
        @Override
        public void onOpened(CameraDevice device) {
            Log.i("front", "FRONT CAMERA STATE CALLBACK ON OPENED");
            frontCameraDevice = device;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
        }
    };

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //Log.i("front", "ON IMAGE AVAILABLE");
            image = frontImageReader.acquireLatestImage();

            ByteBuffer buffer = null;
            byte[] bytes;
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();
            try {
                buffer = image.getPlanes()[0].getBuffer();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            fname = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pic" + frontCameraID + "_" + frontCounter + ".jpg";
            imageFile = new File(fname);
            if(buffer != null){
                bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                try {
                    //save(bytes, file); // save image here
                    OutputStream output = null;
                    output = new FileOutputStream(imageFile);
                    output.write(bytes);
                    frontCounter++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(image != null){
                image.close();
            }
        }
    };

    private void createCaptureSession() {
        Log.i("front", "CREATE CAPTURE SESSION");
        List<Surface> outputSurfaces = new LinkedList<>();
        outputSurfaces.add(frontImageReader.getSurface());

        try {

            frontCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    frontCameraCaptureSession = session;
                    createCaptureRequest();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, frontHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void createCaptureRequest() {
        Log.i("front", "CREATE CAPTURE REQUEST");
        try {

            CaptureRequest.Builder requestBuilder = frontCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            requestBuilder.addTarget(frontImageReader.getSurface());

            // Focus
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
           // int rotation = windowManager.getDefaultDisplay().getRotation();
            //int rotation = getWindowManager().getDefaultDisplay().getRotation();
           // int rotation = 270;
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(270));

            mFrontCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING);
          //  frontCameraCaptureSession.capture(requestBuilder.build(), mFrontCaptureCallback, null);
            frontCameraCaptureSession.setRepeatingRequest(requestBuilder.build(), mFrontCaptureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    PictureCaptureCallback mFrontCaptureCallback = new PictureCaptureCallback() {

        @Override
        public void onPrecaptureRequired() {
            Log.i("front", "ON PRECAPTURE REQUIRED");
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(PictureCaptureCallback.STATE_PRECAPTURE);
            try {
                frontCameraCaptureSession.capture(mPreviewRequestBuilder.build(), this, frontHandler);
               // frontCameraCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), this, frontHandler);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                        CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to run precapture sequence.", e);
            }
        }

        @Override
        public void onReady() {
            //captureStillPicture();
        }

    };
}

