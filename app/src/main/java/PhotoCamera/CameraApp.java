package PhotoCamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;

public class CameraApp {

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
    }
}
