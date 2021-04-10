package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class PedestrianService extends Service {
    public IBinder mBinder = new PedestrianService.LocalBinder();
    private static final String TAG = "PEDESTRIAN SERVICE";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public PedestrianService getServerInstance(){return PedestrianService.this;}
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
