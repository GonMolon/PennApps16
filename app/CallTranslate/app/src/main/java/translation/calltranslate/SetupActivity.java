package translation.calltranslate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        context = this;

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }

        final EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);

        Button continueButton = (Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim().replaceAll("\\D", "");
                if (phoneNumber.length() == 10) {
                    SharedPreferences prefs = context.getSharedPreferences("translation.calltranslate", MODE_PRIVATE);
                    prefs.edit().putString("phoneNumber", phoneNumber).apply();
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
