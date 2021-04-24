package CameraApp;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CrashService extends Service {
    private static final String TAG = "CS_Test";
    private static final String CHANNEL_1_ID = "channel1";
    public IBinder mBinder = new CrashService.LocalBinder();
    private NotificationManagerCompat notificationManager;
    private Context mContext;
    private double speed;

    public CrashService() {
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location.hasSpeed()){
                speed = location.getSpeed()*3.6;
                Log.d(TAG, " get speed method: " + speed);
                hizGoster();
            }
                /*
                if (lastLocation == null) {
                    lastLocation = location;
                }
                distanceInMetres += location.distanceTo(lastLocation);

                 */
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    /*
    DistanceTravelBinder mDistanceTravelBinder = new DistanceTravelBinder();
    static double distanceInMetres;
    static Location lastLocation = null;
    */
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Log.d(TAG, "onCreate");
        //Toast.makeText(getApplicationContext(), TAG, Toast.LENGTH_SHORT).show();
        createNotificationChannels();
        notificationManager = NotificationManagerCompat.from(this);


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "permission yok");
            return;
        }
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
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
        String message = "Message" + speed;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(com.example.saveandroid.R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(1, notification);
    }
    private int prevSpeed = -1;
    public void hizGoster() {
        int ispeed = (int) speed;
        if(prevSpeed != ispeed){
            Intent i = new Intent("cs_Message");
            i.putExtra("hiz", ispeed);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
            prevSpeed = ispeed;
        }

    }
}