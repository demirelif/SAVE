package PhotoCamera;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.example.saveandroid.R;

import java.util.concurrent.TimeUnit;

public class CameraApp {

    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";

    private String cameraOn = CAMERA_BACK;
    private TextureView mTextureView;

    private HandlerThread backCamThread = null;

    public void startCameras(Activity activity){
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] idList = manager.getCameraIdList();
            int maxCameraCnt = idList.length;
            for (int index = 0; index < maxCameraCnt; index++) {
                String cameraId = manager.getCameraIdList()[index];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // not sure about the code above

        String frontID = getFrontFacingCameraId(manager);
        String backID = getBackFacingCameraId(manager);



    }

    public void startBackCamera(){
        backCamThread = new HandlerThread("BackCamThread");
        backCamThread.start();
    }
    public void stopBackCamera() throws InterruptedException {
        backCamThread.quitSafely();
        backCamThread.join();
        backCamThread = null;
    }

    private String getFrontFacingCameraId(CameraManager cManager) {
        try {
            String cameraId;
            int cameraOrientation;
            CameraCharacteristics characteristics;
            for (int i = 0; i < cManager.getCameraIdList().length; i++) {
                cameraId = cManager.getCameraIdList()[i];
                characteristics = cManager.getCameraCharacteristics(cameraId);
                cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            String cameraId;
            int cameraOrientation;
            CameraCharacteristics characteristics;
            for (int i = 0; i < cManager.getCameraIdList().length; i++) {
                cameraId = cManager.getCameraIdList()[i];
                characteristics = cManager.getCameraCharacteristics(cameraId);
                cameraOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


}
