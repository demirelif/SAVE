package SpeechRecognition;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import CameraApp.FatigueService;
import com.example.saveandroid.MainActivity;

public class Speech extends Service {
    private static final String TAG = "SPEECH";
    public IBinder mBinder = new Speech.LocalBinder();

    private static TextToSpeech textToSpeech = MainActivity.tts;
    private boolean isInit;
    private Handler handler;
    private String word;

    public Speech() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();

        Toast.makeText(getApplicationContext(), TAG + " onCreate", Toast.LENGTH_SHORT).show();
        // readText("This is speech");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacksAndMessages(null);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            stopTextReader();
        }
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public Speech getServerInstance() { return Speech.this; }
    }
    /**
    public static void startSpeech(){
        MainActivity.speechRecognizer.startListening(MainActivity.intentRecognizer);
    }

    public static void stopSpeech(){
        MainActivity.speechRecognizer.stopListening();
    }*/

    public static String getSpeech(){
        return MainActivity.speechString;
    }

    public static void readText(String text){
        if ( text != null && textToSpeech != null && MainActivity.textToSpeechIsInitialized ){
            textToSpeech.setPitch(1);
            textToSpeech.setSpeechRate(1);
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null, null);
        }
        else {
            Log.e(TAG, "Something is wrong");
        }
    }

    public void stopTextReader(){
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}