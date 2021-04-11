package CameraApp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Random;


import static CameraApp.CameraService.queue;

public class rPPGService extends Service {
    public IBinder mBinder = new rPPGService.LocalBinder();
    private static final String TAG = "rPPG SERVICE";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public rPPGService getServerInstance(){return rPPGService.this;}
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

        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    consumer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t3.start();

/*        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return super.onStartCommand(intent, flags, startId);
    }

    private static void consumer() throws InterruptedException {
        Random random = new Random();
        while (true){
            Thread.sleep(500);
            Integer value = queue.take();
            Log.i(TAG, "Taken value: " + value + "; Queue size is: " + queue.size());
/*            if(random.nextInt(10) == 0){
                Integer value = queue.take();
                System.out.println("Taken value: " + value + "; Queue size is: " + queue.size());
            }*/
        }
    }
}
