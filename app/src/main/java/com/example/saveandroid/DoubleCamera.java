package com.example.saveandroid;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class DoubleCamera extends AppCompatActivity {
    private static final String TAG = "AndroidCameraApi";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;

    // by elif
    private String frontCameraID;
    private String backCameraID;
    private ImageReader frontImageReader;
    private ImageReader backImageReader;
    private CameraDevice frontCameraDevice;
    private CameraDevice backCameraDevice;
    private CameraCaptureSession frontCameraCaptureSession;
    private CameraCaptureSession backCameraCaptureSession;

    private WindowManager windowManager;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Handler frontHandler;
    private HandlerThread frontThread;
    private int frontCounter = 0;
    private int backCounter = 0;


    CaptureRequest.Builder mPreviewRequestBuilder;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }

        Log.i("camera", "on create starts");
        setCamera();
        Log.i("camera", "set camera done");
        int photoCounter = 0;
        while ( photoCounter < 10 ){
            photoCounter++;
            openCamera();
        }
        Log.i("camera", "open camera done");
    }

    private void setCamera() {
        Log.i("camera", "set camera icindeyiz");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                // getting two cameras here
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    this.frontCameraID = cameraID;
                } else if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    this.backCameraID = cameraID;
                } else {
                    continue;
                }
            }
            // WARNING - THIS PART SHOULD CHANGE, MUST NOT BE CONSTANT?
            int pictureWidth = 640;
            int pictureHeight = 480;
            // END OF THE WARNING

            // max images should not be 10
            frontImageReader = ImageReader.newInstance(pictureWidth, pictureHeight, ImageFormat.JPEG, 10);
            frontImageReader.setOnImageAvailableListener(onImageAvailableListener, frontHandler);

            backImageReader = ImageReader.newInstance(pictureWidth, pictureHeight, ImageFormat.JPEG, 10);
            backImageReader.setOnImageAvailableListener(backOnImageAvailableListener, backgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if ( frontCameraID == null ){ Log.e("camera", "cannot find camera"); }
        else
            Log.i("camera", "set camera success");

    }

    private void openCamera() {
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

        try {
            manager.openCamera(backCameraID, backCameraStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private final CameraDevice.StateCallback frontCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice device) {
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

    private final CameraDevice.StateCallback backCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice device) {
            backCameraDevice = device;

            backCreateCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
        }
    };

    private void createCaptureSession() {
        List<Surface> outputSurfaces = new LinkedList<>();
        outputSurfaces.add(frontImageReader.getSurface());

        try {

            frontCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    frontCameraCaptureSession = session;
                    for (int i = 0; i < 3; i++)
                        createCaptureRequest();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void backCreateCaptureSession() {
        List<Surface> outputSurfaces = new LinkedList<>();
        outputSurfaces.add(backImageReader.getSurface());

        try {

            backCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    backCameraCaptureSession = session;
                    for (int i = 0; i < 3; i++)
                        backCreateCaptureRequest();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = frontImageReader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            String fname = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pic" + frontCameraID + "_" + frontCounter + ".jpg";
            Log.d(TAG, "Saving:" + fname);
            File file = new File(fname);
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            try {
                //save(bytes, file); // save image here
                OutputStream output = null;
                output = new FileOutputStream(file);
                output.write(bytes);
                frontCounter++;

            } catch (IOException e) {
                e.printStackTrace();
            }
            image.close();
        }
    };

    private final ImageReader.OnImageAvailableListener backOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = backImageReader.acquireLatestImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            String fname = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pic" + backCameraID + "_" + backCounter + ".jpg";
            Log.d(TAG, "Saving:" + fname);
            File file = new File(fname);
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            try {
                //save(bytes, file); // save image here
                OutputStream output = null;
                output = new FileOutputStream(file);
                output.write(bytes);
                backCounter++;

            } catch (IOException e) {
                e.printStackTrace();
            }
            image.close();
        }
    };


    private void createCaptureRequest() {
        try {

            CaptureRequest.Builder requestBuilder = frontCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            requestBuilder.addTarget(frontImageReader.getSurface());

            // Focus
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            mFrontCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING);
            frontCameraCaptureSession.capture(requestBuilder.build(), mFrontCaptureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void backCreateCaptureRequest() {
        try {

            CaptureRequest.Builder requestBuilder = backCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            requestBuilder.addTarget(backImageReader.getSurface());

            // Focus
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            mBackCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING);
            backCameraCaptureSession.capture(requestBuilder.build(), mBackCaptureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    PictureCaptureCallback mFrontCaptureCallback = new PictureCaptureCallback() {

        @Override
        public void onPrecaptureRequired() {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(STATE_PRECAPTURE);
            try {
                frontCameraCaptureSession.capture(mPreviewRequestBuilder.build(), this, null);
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

    PictureCaptureCallback mBackCaptureCallback = new PictureCaptureCallback() {

        @Override
        public void onPrecaptureRequired() {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            setState(STATE_PRECAPTURE);
            try {
                backCameraCaptureSession.capture(mPreviewRequestBuilder.build(), this, null);
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

abstract class PictureCaptureCallback
        extends CameraCaptureSession.CaptureCallback {

    static final int STATE_PREVIEW = 0;
    static final int STATE_LOCKING = 1;
    static final int STATE_LOCKED = 2;
    static final int STATE_PRECAPTURE = 3;
    static final int STATE_WAITING = 4;
    static final int STATE_CAPTURING = 5;

    private int mState;

    PictureCaptureCallback() {
    }

    void setState(int state) {
        mState = state;
    }

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
        process(partialResult);
    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        process(result);
    }

    private void process(@NonNull CaptureResult result) {
        switch (mState) {
            case STATE_LOCKING: {
                Integer af = result.get(CaptureResult.CONTROL_AF_STATE);
                if (af == null) {
                    break;
                }
                if (af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                        af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                    Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        setState(STATE_CAPTURING);
                        onReady();
                    } else {
                        setState(STATE_LOCKED);
                        onPrecaptureRequired();
                    }
                }
                break;
            }
            case STATE_PRECAPTURE: {
                Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        ae == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED ||
                        ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    setState(STATE_WAITING);
                }
                break;
            }
            case STATE_WAITING: {
                Integer ae = result.get(CaptureResult.CONTROL_AE_STATE);
                if (ae == null || ae != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                    setState(STATE_CAPTURING);
                    onReady();
                }
                break;
            }
        }
    }

    /**
     * Called when it is ready to take a still picture.
     */
    public abstract void onReady();

    /**
     * Called when it is necessary to run the precapture sequence.
     */
    public abstract void onPrecaptureRequired();

}
