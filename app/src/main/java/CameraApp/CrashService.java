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
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CrashService extends Service {
    private static final String TAG = "CS_Test";
    private static final String CHANNEL_1_ID = "channel1";
    int sayac=0;
    public IBinder mBinder = new CrashService.LocalBinder();
    private NotificationManagerCompat notificationManager;
    private static Timer mTimer = null;

    public CrashService() {
    }

    private class TakvimKontrol extends TimerTask {
        public void run() {
            sayac++;
            msgGoster();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        //Toast.makeText(getApplicationContext(), TAG, Toast.LENGTH_SHORT).show();
        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this);

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TakvimKontrol(), 50, 20000);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public CrashService getServiceInstance() {
            return CrashService.this;
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

    public void msgGoster() {
        String title = "Title";
        String message = "Message" + sayac;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(com.example.saveandroid.R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1, notification);
    }
}