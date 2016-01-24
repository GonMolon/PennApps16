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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;

public class ReceivingCall extends AppCompatActivity {

    static final int border = 20;
    private ImageView decline_call;
    private ImageView accept_call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_call);
        getSupportActionBar().setTitle("Incoming call");

        Bundle b = getIntent().getExtras();
        final String id = b.getString("message_id");

        if(id == null) {
            finish();
        }

        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        final Ringtone defaultRingtone = RingtoneManager.getRingtone(this, defaultRingtoneUri);
        defaultRingtone.play();

        final Activity activity = this;

        final Firebase db = new Firebase("https://vivid-inferno-6896.firebaseio.com/");
        db.child(id).child("finished").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getKey() == "finished") {
                    if((boolean) dataSnapshot.getValue()) {
                        defaultRingtone.stop();
                        activity.finish();
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

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
                if (seekBar.getProgress() < border) {
                    db.child(id).child("finished").setValue(true);
                    defaultRingtone.stop();
                    activity.finish();
                } else if (seekBar.getProgress() > 100 - border) {
                    defaultRingtone.stop();
                    db.child(id).child("language2").setValue(Locale.getDefault().getLanguage());
                    Intent intent = new Intent(activity.getApplicationContext(), CallActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("user", 2);
                    startActivityForResult(intent, 1);
                    seekBar.setProgress(50);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1) {
            finish();
        }
    }
}
