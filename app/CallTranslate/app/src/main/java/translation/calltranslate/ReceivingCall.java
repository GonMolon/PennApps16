package translation.calltranslate;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class ReceivingCall extends AppCompatActivity {

    static final int border = 20;
    private ImageView decline_call;
    private ImageView accept_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_call);
        getSupportActionBar().setTitle("Incoming call");

        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        Ringtone defaultRingtone = RingtoneManager.getRingtone(this, defaultRingtoneUri);
        defaultRingtone.play();

        final Activity activity = this;

        decline_call = (ImageView)findViewById(R.id.decline_call);
        accept_call = (ImageView)findViewById(R.id.accept_call);
        TextView input_phone = (TextView)findViewById(R.id.input_phone);
        input_phone.setText("123-456-7890");
        SeekBar selector = (SeekBar)findViewById(R.id.accept_call_bar);
        selector.setProgress(50);
        selector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress < border) {
                        decline_call.setImageResource(R.drawable.glowpad_decline_activated);
                    } else {
                        decline_call.setImageResource(R.drawable.glowpad_decline_normal);
                    }
                    if (progress > 100 - border) {
                        accept_call.setImageResource(R.drawable.glowpad_accept_activated);
                    } else {
                        accept_call.setImageResource(R.drawable.glowpad_accept_normal);
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getProgress() < border) {
                    activity.finish();
                } else if(seekBar.getProgress() > 100-border) {
                    Intent intent = new Intent(activity.getApplicationContext(), SpeechActivity.class);
                    startActivity(intent);
                    activity.finish();
                } else {
                    seekBar.setProgress(50);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
    }
}
