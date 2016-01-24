package translation.calltranslate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

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
        startService(new Intent(getBaseContext(), ReceivingCallListener.class)); //service to check if i'm being called

        context = this;
        prefs = this.getSharedPreferences("translation.calltranslate", MODE_PRIVATE);

        if (prefs.getString("phoneNumber", "None").equals("None")) {
            launchPhoneRequest();
        }

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setElevation(0);
            ab.setTitle("Translated Talk");
        }

        ImageButton phoneButton = (ImageButton) findViewById(R.id.imageButton);
        phoneButton.setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETUP_ACTIVITY_REQ_CODE) {
            if (resultCode != RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_contacts:
                Intent contactsIntent = new Intent(context, ContactActivity.class);
                startActivity(contactsIntent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
