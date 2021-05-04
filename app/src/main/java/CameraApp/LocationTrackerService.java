package CameraApp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.saveandroid.MainActivity;
import com.example.saveandroid.R;

import SpeechRecognition.Speech;

public class LocationTrackerService extends Service implements ILocationTrackerCallBack {
    public static final String CHANNEL_LOCATIONTRACKER = "RouteTrackingChannel";
    public static NotificationManager notificationManager = null;
    private static LocationTrackerService crashService = null;
    private static boolean crashServiceConnected = false;
    static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            crashService = ((LocationTrackerService.LocalBinder) iBinder).getServiceInstance();
            crashServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            crashService = null;
            crashServiceConnected = false;
        }
    };
    private static FloatingIcon floatingIcon = null;
    public IBinder mBinder = new LocationTrackerService.LocalBinder();
    public com.example.saveandroid.LocationTracker locationTracker;
    double prevSpeed = -1;
    private Context mContext;
    private float MIN_METER = 1;  /* 10 secs */
    private long FASTEST_INTERVAL = 200; /* 2 sec */
    private boolean isInDanger = false;
    private CustomDialogBox customDialogBox = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void StartLocationTracking(Context context) {
        notificationManager = context.getSystemService(NotificationManager.class);
        createNotChan();
        StartLocationService(context);
    }

    public static void StopLocationTracking(Context context) {
        if (crashService != null) {
            crashService.locationTracker.StopTracking();
            if (floatingIcon != null) {
                floatingIcon.Remove();
                floatingIcon = null;
            }
        }
    }

    public static void StartLocationService(Context context) {
        Intent crashServiceIntent = new Intent(context, LocationTrackerService.class);
        context.bindService(crashServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        ContextCompat.startForegroundService(context, crashServiceIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotChan() {
        NotificationChannel serChannel = new NotificationChannel(CHANNEL_LOCATIONTRACKER, "Routing", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(serChannel);
        Log.d(MainActivity.TAG, "channel created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(MainActivity.TAG, "onBind");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_LOCATIONTRACKER)
                .setContentTitle("Route Tracker")
                .setContentText("Tracking....")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        CreateLocationTracker(mContext);
        return START_NOT_STICKY;
    }

    private void CreateLocationTracker(Context context) {
        try {
            locationTracker = new com.example.saveandroid.LocationTracker(context, com.example.saveandroid.LocationTracker.Method.Location);
        } catch (Exception ex) {
            Log.e(MainActivity.TAG, "LocationTracker Creation Exception");
        }

        try {
            locationTracker.StartTracking(FASTEST_INTERVAL, MIN_METER, this);
        } catch (Exception e) {
            Log.e(MainActivity.TAG, "LocationTracker Start Exception");
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Toast.makeText(getApplicationContext(), MainActivity.TAG, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(MainActivity.TAG, "onDestroy");
        if (locationTracker != null)
            locationTracker.StopTracking();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasSpeed()) {
            double speed = location.getSpeed() * 3.6;
            if (prevSpeed > 0) {
                double hiz = ((int) (speed * 10)) / 10.0;

                String msg = "Location changed and speed is " + Double.toString(hiz);
                Log.i(MainActivity.TAG, msg);
                if (prevSpeed > speed * 1.2) {
                    SetStateToDangerMode();
                }
            }
            prevSpeed = speed;
        } else {
            Log.i(MainActivity.TAG, "Location has no speed. Previous alt: " + Double.toString(location.getLatitude()) + "long: " + Double.toString(location.getLongitude()));
        }
    }

    private void SetStateToDangerMode() {
        if (!isInDanger) {
            isInDanger = true;
            DisplayIcon();
            DisplayDialog();
            Speech.readText("Collision Warning");
        }
    }

    private void SetStateToSafeMode() {
        if (isInDanger) {
            isInDanger = false;
            if (floatingIcon != null)
                floatingIcon.Remove();
            if (customDialogBox != null)
                customDialogBox.Remove();
        }
    }

    private void DisplayIcon() {
        floatingIcon = new FloatingIcon(this);
    }

    private void DisplayDialog() {
        if (customDialogBox == null)
            customDialogBox = new CustomDialogBox(this, new ICustomDialogListener() {
                @Override
                public void onClick(View view) {
                    onFloatingIconClicked(view);
                }

                @Override
                public void dlgTimeOut() {
                    checkTimeOut();
                }
            }, DialogType.AbnormalBPM, 8);
    }

    public void onFloatingIconClicked(View view) {
        if (view.getId() == R.id.csbubbleimg)
            DisplayDialog();
        else
            SetStateToSafeMode();
    }

    public void checkTimeOut() {
        if (customDialogBox != null) {
            customDialogBox.Remove();
            customDialogBox = null;
        }
    }

    public class LocalBinder extends Binder {
        public LocationTrackerService getServiceInstance() {
            return LocationTrackerService.this;
        }
    }
}
