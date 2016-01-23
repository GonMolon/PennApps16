package translation.calltranslate;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class AddChatActivity extends AppCompatActivity {

    private static final String TAG = "Chat";

    private Context context;
    private EditText to_phone;
    private VoiceSynthesizer tts;
    private int CALL_REQ_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);

        setTitle("New call");

        context = this;

//        tts = new VoiceSynthesizer(context);

        to_phone = (EditText)findViewById(R.id.editText);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tts.finish();
                finish();
            }
        });

        Button addChatButton = (Button) findViewById(R.id.addChatButton);
        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Chat started", Toast.LENGTH_SHORT).show();
//                FirebaseChat chat = new FirebaseChat(to_phone.getText().toString(), getApplicationContext(), new FirebaseChat.OnNewMessageListener() {
//                    @Override
//                    public void onNewMessage(DataSnapshot dataSnapshot) {
//                        Log.d(TAG, "MESSAGE RECEIVED");
//                        String text = (String) dataSnapshot.child("text").getValue();
//                        Log.d(TAG, text);
//                        tts.speak(text);
//                    }
//                });
//                chat.send_message("Hola, ¿qué tal?");
                Intent callIntent = new Intent(context, CallActivity.class);
                startActivityForResult(callIntent, 200);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        tts.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
