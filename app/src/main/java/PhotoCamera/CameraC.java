package PhotoCamera;

import android.content.Context;
import android.hardware.camera2.CameraManager;

public class CameraC {
    CameraManager Object cameraManager;
    private Object CameraManager;

    public void onCreate(){
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }
}
