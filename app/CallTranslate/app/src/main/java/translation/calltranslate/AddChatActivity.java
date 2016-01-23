package translation.calltranslate;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class AddChatActivity extends AppCompatActivity {

    private static final String TAG = "Chat";

    private Context context;
    private EditText to_phone;
    private VoiceSynthesizer tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);

        context = this;

        tts = new VoiceSynthesizer(context);

        to_phone = (EditText)findViewById(R.id.editText);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.finish();
                finish();
            }
        });

        Button addChatButton = (Button) findViewById(R.id.addChatButton);
        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Chat started", Toast.LENGTH_SHORT).show();
                FirebaseChat chat = new FirebaseChat(to_phone.getText().toString(), getApplicationContext(), new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "MESSAGE RECEIVED");
                        tts.speak((String) dataSnapshot.child("text").getValue());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                chat.send_message("Hola, ¿qué tal?");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tts.finish();
    }
}
