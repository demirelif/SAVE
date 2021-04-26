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
    private static SpeechRecognizer speechRecognizer;
    private static TextToSpeech textToSpeech = MainActivity.tts;
    private static Intent intentRecognizer;
    private String speechString;
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

        Toast.makeText(getApplicationContext(),TAG + " onCreate", Toast.LENGTH_SHORT).show();
       // readText("This is speech");
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) { // results when we finish listening
                ArrayList<String> matches =  results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String s = "";
                if ( matches!= null ){
                    for ( int i = 0; i < matches.size(); i++){
                        s += matches.get(i);
                    }
                }
                speechString = s;
            }

            @Override
            public void onPartialResults(Bundle partialResults) { // results before finishing

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        if ( speechRecognizer != null ){
            Log.i(TAG, "speech recognizer ");
        }
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

    public static void startSpeech(){
        speechRecognizer.startListening(intentRecognizer);
    }

    public static void stopSpeech(){
        speechRecognizer.stopListening();
    }

    public String getSpeech(){
        return speechString;
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
      //  textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);
        /*
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if ( status == TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if ( result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e(TAG, "Language not supported.");
                    }
                    else {
                        Log.i(TAG, "Success.");

                    }
                }
                else {
                    Log.e(TAG, "Initialization failed.");
                }

            }
        });

         */

    }

    public void stopTextReader(){
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}