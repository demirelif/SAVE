package SpeechRecognition;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import CameraApp.FatigueService;

public class Speech extends Service {
    private static final String TAG = "SPEECH";
    public IBinder mBinder = new Speech.LocalBinder();
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Intent intentRecognizer;
    private String speechString;

    public Speech() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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
        return mBinder;
    }
    public class LocalBinder extends Binder {
        public Speech getServerInstance() { return Speech.this; }
    }

    public void startSpeech(){
        speechRecognizer.startListening(intentRecognizer);
    }

    public void stopSpeech(){
        speechRecognizer.stopListening();
    }

    public String getSpeech(){
        return speechString;
    }

    public void readText(String text){
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
    }

    public void stopTextReader(){
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}