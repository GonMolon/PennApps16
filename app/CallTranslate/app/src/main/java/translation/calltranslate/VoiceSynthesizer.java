package translation.calltranslate;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class VoiceSynthesizer implements TextToSpeech.OnInitListener {

    private static final String TAG = "TTS";
    private TextToSpeech tts;
    private boolean ready;
    private boolean error;
    private int id;

    public VoiceSynthesizer(Context context) {
        id = 1;
        ready = false;
        error = false;
        //its supposed that the required language is always the locale one, so no changes need to be done
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        Log.i(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            ready = true;
        } else {
            Log.e(TAG, "Initialization Failed");
            error = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void speak(String text) {
        if(error || !ready) {
            Log.e(TAG, "Error in speaking");
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, String.valueOf(id));
            while(tts.isSpeaking());
            ++id;
        }
    }

    public boolean is_ready() {
        return ready;
    }

    public void finish() {
        tts.stop();
        tts.shutdown();
    }
}