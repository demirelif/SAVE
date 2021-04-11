package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CameraService extends Service {
    //private static final String TAG = "AndroidCameraApi";
    private static final String TAG = "CAMERA SERVICE";
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_PERMISSION = 200;
    public static BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(10);
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
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    producer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();

/*        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        return super.onStartCommand(intent, flags, startId);
    }

    private static void producer() throws InterruptedException {
        Random random = new Random();
        while (true){
            Thread.sleep(500);
            int value = random.nextInt(100);
            queue.put(value);
            Log.i(TAG, "Inserting value: " + value + "; Queue size is: " + queue.size());
        }
    }




}
