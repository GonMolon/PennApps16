package translation.calltranslate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TTSActivity extends AppCompatActivity {

    private VoiceSynthesizer vs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        vs = new VoiceSynthesizer(getApplicationContext());

        Button test = (Button)findViewById(R.id.tts);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vs.is_ready()) {
                    vs.speak("Hello, TTS is working!");
                }
            }
        });
    }
}
