package translation.calltranslate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.client.Firebase;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences prefs;
    private int SETUP_ACTIVITY_REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this); //firebase stuff

        context = this;
        prefs = this.getSharedPreferences("translation.calltranslate", MODE_PRIVATE);

        if (prefs.getString("phoneNumber", "None").equals("None")) {
            launchPhoneRequest();
        }

        Button speechTestButton = (Button) findViewById(R.id.speechTestButton);
        speechTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SpeechActivity.class);
                startActivity(intent);
            }
        });

        Button tts = (Button)findViewById(R.id.open_test);
        tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, TTSActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addChatIntent = new Intent(context, AddChatActivity.class);
                startActivity(addChatIntent);
            }
        });
    }

    private void launchPhoneRequest() {
        Intent getPhoneNumberIntent = new Intent(context, SetupActivity.class);
        startActivityForResult(getPhoneNumberIntent, SETUP_ACTIVITY_REQ_CODE);
    }

    private void getMyChats() {
        // TODO: Search firebase using my phone number for all my chats, then add them to the
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETUP_ACTIVITY_REQ_CODE) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }
}
