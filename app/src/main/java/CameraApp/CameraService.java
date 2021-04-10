package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class CameraService extends Service {
    //private static final String TAG = "AndroidCameraApi";
    private static final String TAG = "CAMERA SERVICE";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;

    public IBinder mBinder = new CameraService.LocalBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public CameraService getServerInstance(){return CameraService.this;}
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
        return super.onStartCommand(intent, flags, startId);
    }




}
