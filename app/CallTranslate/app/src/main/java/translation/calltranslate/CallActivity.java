package translation.calltranslate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallActivity extends AppCompatActivity implements RecognitionListener {

    private Context context;
    private SharedPreferences prefs;
    public static final String TAG = CallActivity.class.getSimpleName();
    private final OkHttpClient client = new OkHttpClient();
    private String microsoftAuthUrl = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    private String myNumber;
    private String otherNumber;
    private String myLanguage;
    private String otherLanguage;
    private VoiceSynthesizer tts;
    private SpeechRecognizer mSpeechRecognizer = null;
    private Intent mSpeechRecognizerIntent;
    private boolean mIsListening;
    private ArrayList<Integer> voiceLevelChanges;
    private FirebaseChat chat;
    private int user;

    private TextView callProgressTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        context = this;
        prefs = this.getSharedPreferences("translation.calltranslate", MODE_PRIVATE);

        callProgressTextView = (TextView) findViewById(R.id.callProgressText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        myNumber = prefs.getString("phoneNumber", "None");
        String id = "";

        Bundle b = getIntent().getExtras();
        if (b != null) {
            user = b.getInt("user");
            if (user == 1) {
                otherNumber = b.getString("otherNumber");
            } else {
                id = b.getString("id");
                otherNumber = "None";
            }
        } else {
            otherNumber = "None";
            user = 1;
        }

        if (user == 1) {
            callProgressTextView.setText("Calling " + otherNumber);
            myLanguage = Locale.getDefault().getLanguage();
            otherLanguage = "";
            id = myNumber + "_" + otherNumber;
        } else {
            callProgressTextView.setText("Waiting for first message to be sent.");
        }

        tts = new VoiceSynthesizer(context);
        setupSpeechInput();

        Firebase db = new Firebase("https://vivid-inferno-6896.firebaseio.com");
        Firebase callRef = db.child(id);
        if (user == 1) {
            callRef.removeValue();
            callRef = db.child(id);
        } else {
            callRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        switch (child.getKey()) {
                            case "person1":
                                otherNumber = child.getValue().toString();
                                break;
                            case "language1":
                                otherLanguage = child.getValue().toString();
                                break;
                            case "language2":
                                myLanguage = child.getValue().toString();
                                break;
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, firebaseError.getMessage());
                }
            });
        }

        chat = new FirebaseChat(callRef, user, otherNumber, id, context);

        callRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("language2")) {
                    System.out.println("User 2 joined!");
                    otherLanguage = dataSnapshot.getValue().toString();
                    chat.setLang2(otherLanguage);
                    callProgressTextView.setText("Call has begun. Begin speaking.");
                    progressBar.setVisibility(View.GONE);
                    listen();
                } else if (dataSnapshot.getKey().equals("finished")) {
                    if ((boolean) dataSnapshot.getValue()) {
                        Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show();
                        wrapUp();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        callRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if ((dataSnapshot.child("to").getValue()).toString().equals(myNumber) && !((boolean) dataSnapshot.child("read").getValue())) {
                    Log.d(TAG, "MESSAGE RECEIVED");
                    String text = (String) dataSnapshot.child("text").getValue();
                    Log.d(TAG, text);
                    dataSnapshot.child("read").getRef().setValue(true);
                    sayMessage(text);
                    listen();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        wrapUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wrapUp();
    }

    private void wrapUp() {
        tts.finish();
        mSpeechRecognizer.destroy();
        chat.finish_conversation();
        finish();
    }

    private void getTranslation(final String message) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id", "PennApps")
                .add("client_secret", "yR8VRcs+MsUPiqt7ee9IipEcoy03Bs35mvZbSGFtZ2o=")
                .add("scope", "http://api.microsofttranslator.com")
                .add("grant_type", "client_credentials")
                .build();
        Request request = new Request.Builder()
                .url(microsoftAuthUrl)
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Request was successful
                    try {
                        JSONObject responseObj = new JSONObject(response.body().string());
                        String accessToken = responseObj.getString("access_token");

                        String uri = Uri.parse("http://api.microsofttranslator.com/v2/Http.svc/Translate?")
                                .buildUpon()
                                .appendQueryParameter("text", message)
                                .appendQueryParameter("from", myLanguage)
                                .appendQueryParameter("to", otherLanguage)
                                .build().toString();

                        String header = "Bearer " + accessToken;

                        Request request = new Request.Builder()
                                .url(uri)
                                .header("Authorization", header)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);

                                try {
                                    String xmlResponse = response.body().string();
                                    DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                                    Document parse = newDocumentBuilder.parse(new ByteArrayInputStream(xmlResponse.getBytes()));
                                    String translatedMessage = parse.getFirstChild().getTextContent();
                                    System.out.println(translatedMessage);
                                    chat.send_message(translatedMessage);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callProgressTextView.setText("Message sent. Please wait for a response.");
                                        }
                                    });
                                } catch (ParserConfigurationException e) {
                                    e.printStackTrace();
                                } catch (SAXException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Request not successful
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    private void sayMessage(String message) {
        tts.speak(message);
    }

    protected void setupSpeechInput() {
        voiceLevelChanges = new ArrayList<>();
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

    private void listen() {
        if (!mIsListening) {
            System.out.println("Started listening");
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            voiceLevelChanges.clear();
            voiceLevelChanges.addAll(Arrays.asList(90, 90, 90, 90, 90));
//            recordCircle.setImageResource(R.drawable.record_circle_active);
        } else {
            mSpeechRecognizer.stopListening();
        }
        mIsListening = !mIsListening;
    }

    /**
     * Methods for RecognitionListener
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.d(TAG, "onBeginingOfSpeech");
        callProgressTextView.setText(getString(R.string.now_recording));
//        recordCircle.getLayoutParams().width = 90;
//        recordCircle.getLayoutParams().height = 90;
//        recordCircle.requestLayout();
//        recordCircle.setImageResource(R.drawable.record_circle_active);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech");
        callProgressTextView.setText(getString(R.string.done_recording));
//        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.requestLayout();
//        recordCircle.setImageResource(R.drawable.record_circle_inactive);
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
//                recordCircle.setImageResource(R.drawable.record_circle_inactive);
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
//                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                mError = "Insufficient permissions";
                mSpeechRecognizer.cancel();
                mIsListening = false;
//                recordCircle.setImageResource(R.drawable.record_circle_inactive);
                break;
        }
        Log.i(TAG,  "Error: " +  error + " - " + mError);

//        micText.setText(mError);
//        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.requestLayout();
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
        callProgressTextView.setText(getString(R.string.begin_recording));
//        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.requestLayout();
    }

    @Override
    public void onResults(Bundle results) {
        mIsListening = false;
//        micText.setText(getString(R.string.tap_on_mic));
//        recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//        recordCircle.requestLayout();
//        recordCircle.setImageResource(R.drawable.record_circle_inactive);
        // Log.d(TAG, "onResults"); //$NON-NLS-1$
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        // matches are the return values of speech recognition engine
        if (matches != null) {
            // Log.d(TAG, matches.toString()); //$NON-NLS-1$
            System.out.println(matches.get(0));
            callProgressTextView.setText("Sending message...");
            try {
                getTranslation(matches.get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sorry, we couldn't understand you. Try again.", Toast.LENGTH_SHORT).show();
            callProgressTextView.setText("Begin speaking.");
            listen();
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

//        if (!mIsListening) {
//            recordCircle.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//            recordCircle.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
//            recordCircle.setImageResource(R.drawable.record_circle_inactive);
//        } else {
//            recordCircle.getLayoutParams().width = adjustedSize;
//            recordCircle.getLayoutParams().height = adjustedSize;
//        }
//        recordCircle.requestLayout();
    }
}
