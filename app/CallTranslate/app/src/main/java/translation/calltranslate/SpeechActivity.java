package translation.calltranslate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;

public class SpeechActivity extends AppCompatActivity implements RecognitionListener {

    private String apiURL = "http://url.com/translate";
    public static final String TAG = MainActivity.class.getSimpleName();
    private SpeechRecognizer mSpeechRecognizer = null;
    private Intent mSpeechRecognizerIntent;
    private boolean mIsListening;
    private ArrayList<Integer> voiceLevelChanges;

    private ImageButton recordButton;
    private TextView micText;
    private ImageView recordCircle;

    private SharedPreferences prefs;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        context = this;

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setElevation(0);
            ab.setDisplayShowTitleEnabled(false);
        }

        prefs = this.getSharedPreferences("translation.calltranslate", MODE_PRIVATE);

        recordButton = (ImageButton) findViewById(R.id.recordButton);
        micText = (TextView) findViewById(R.id.micText);
        recordCircle = (ImageView) findViewById(R.id.recordCircle);

        voiceLevelChanges = new ArrayList<>();

        setupSpeechInput();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void listen(View view) {
        if (!mIsListening) {
            System.out.println("Started listening");
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            voiceLevelChanges.clear();
            voiceLevelChanges.addAll(Arrays.asList(90, 90, 90, 90, 90));
            recordCircle.setImageResource(R.drawable.record_circle_active);
        } else {
            mSpeechRecognizer.stopListening();
        }
        mIsListening = !mIsListening;
    }

    protected void setupSpeechInput() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (!mSpeechRecognizerIntent.hasExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE)) {
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        }
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
        mIsListening = false;
    }

    private void callApi(String arabicText) {
        SpannableString ss1=  new SpannableString("Getting match");
        ss1.setSpan(new RelativeSizeSpan(1.7f), 0, ss1.length(), 0);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(ss1);
        progress.setCancelable(false);
        progress.show();

        ByteArrayEntity entity = null;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("arabicText", arabicText);
            jsonParams.put("translation", prefs.getString("translation", "en-hilali"));
        } catch (JSONException je) {
            Log.e(TAG, je.getMessage());
        }
        try {
            entity = new ByteArrayEntity(jsonParams.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ue) {
            Log.e(TAG, ue.getMessage());
        }

        if (entity != null) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(getApplicationContext(), apiURL, entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // called when response HTTP status is "200 OK"
                    progress.dismiss();
                    // unlockScreenOrientation();
                    Log.v(TAG, response.toString());
                    try {
                        JSONObject result = response.getJSONObject("result");
                        if (result.getBoolean("empty")) {
                            Toast.makeText(getApplicationContext(), "No matches were found", Toast.LENGTH_SHORT).show();
                        } else {
//                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                            intent.putExtra("response", result.toString());
////                            intent.putExtra("match", result.getJSONObject("match").toString());
//                            intent.putExtra("query", result.getString("queryText"));
//                            startActivity(intent);
                            System.out.print("Made it to API else case");
                        }
                    } catch (JSONException je) {
                        Log.e("API result problem: ", je.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    progress.dismiss();
                    // unlockScreenOrientation();
                    Log.e("API result problem: ", e.getMessage());
                    Toast.makeText(getApplicationContext(), "Sorry, something went wrong.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    progress.dismiss();
                    // unlockScreenOrientation();

                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        Log.e("API result problem: ", "Socket Timeout");
                        Toast.makeText(getApplicationContext(), "Sorry, server connection lost.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("API result problem: ", errorMessage);
                        Toast.makeText(getApplicationContext(), "Sorry, something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    /**
     * Methods for RecognitionListener
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginingOfSpeech");
        micText.setText(getString(R.string.now_recording));
        recordCircle.getLayoutParams().width = 90;
        recordCircle.getLayoutParams().height = 90;
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_active);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        micText.setText(getString(R.string.done_recording));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_inactive);
    }

    @Override
    public void onError(int error) {
        String mError = "";
        switch (error) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                mError = "Network timeout";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                mError = "Network error";
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                mError = "Audio error";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_SERVER:
                mError = "Server error";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                mError = "Client error";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                mError = "Speech timed out";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                mError = "No match";
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                mError = "Speech recognition busy";
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                mError = "Insufficient permissions";
                mSpeechRecognizer.cancel();
                mIsListening = false;
                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
        }
        Log.i(TAG,  "Error: " +  error + " - " + mError);

        micText.setText(mError);
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        mIsListening = false;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        micText.setText(getString(R.string.begin_recording));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
    }

    @Override
    public void onResults(Bundle results) {
        mIsListening = false;
        micText.setText(getString(R.string.tap_on_mic));
        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        recordCircle.requestLayout();
        recordCircle.setImageResource(R.drawable.record_circle_inactive);
        // Log.d(TAG, "onResults"); //$NON-NLS-1$
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // matches are the return values of speech recognition engine
        if (matches != null) {
            // Log.d(TAG, matches.toString()); //$NON-NLS-1$
//            callApi(matches.get(0));
            System.out.println(matches.get(0));
        } else {
            Toast.makeText(getApplicationContext(), "Sorry, we couldn't understand you.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        if (rmsdB < 0) {
            rmsdB = 0;
        }
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (rmsdB * 8) + 80, getResources().getDisplayMetrics());

        voiceLevelChanges.remove(0);
        voiceLevelChanges.add(size);

        int adjustedSize = 0;

        for (int i = 0; i < voiceLevelChanges.size(); i++) {
            adjustedSize += voiceLevelChanges.get(i);
        }

        adjustedSize = adjustedSize / voiceLevelChanges.size();

        if (!mIsListening) {
            recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
            recordCircle.setImageResource(R.drawable.record_circle_inactive);
        } else {
            recordCircle.getLayoutParams().width = adjustedSize;
            recordCircle.getLayoutParams().height = adjustedSize;
        }
        recordCircle.requestLayout();
    }
}
