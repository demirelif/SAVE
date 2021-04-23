package CameraApp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CrashService extends Service {
    private static final String TAG = "CS_Test";
    private static final String CHANNEL_1_ID = "channel1";

    public IBinder mBinder = new CrashService.LocalBinder();

    public CrashService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Toast.makeText(getApplicationContext(), TAG, Toast.LENGTH_SHORT).show();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public CrashService getServiceInstance(){
            return CrashService.this;
        }
    }
}