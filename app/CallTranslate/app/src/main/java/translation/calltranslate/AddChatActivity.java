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

    private Context context;
    private EditText toPhoneEditText;
    private int CALL_REQ_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);

        setTitle("New call");

        context = this;

        toPhoneEditText = (EditText) findViewById(R.id.editText);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button addChatButton = (Button) findViewById(R.id.addChatButton);
        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = toPhoneEditText.getText().toString();
                if (phoneNumber.length() == 0) {
                    Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show();
                } else {
                    Intent callIntent = new Intent(context, CallActivity.class);
                    callIntent.putExtra("user", 1);
                    callIntent.putExtra("otherNumber", toPhoneEditText.getText().toString());
                    startActivityForResult(callIntent, CALL_REQ_CODE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
