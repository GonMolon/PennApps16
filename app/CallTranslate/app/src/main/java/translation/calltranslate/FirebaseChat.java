package translation.calltranslate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;

public class FirebaseChat {

    private static final String TAG = "Firebase";

    private String myNum;
    private String otherNum;
    private String lang1;
    private String lang2;
    private String id;
    private boolean finished;
    private Firebase callRef;

    public FirebaseChat(Firebase callReference, String toPhone, String callId, Context context, final OnNewMessageListener listener) {
        SharedPreferences prefs = context.getSharedPreferences("translation.calltranslate", Context.MODE_PRIVATE);
        this.myNum = prefs.getString("phoneNumber", null);
        this.otherNum = toPhone;
        this.id = callId;
        this.lang1 = Locale.getDefault().getLanguage();
        this.lang2 = "";
        this.finished = false;
        this.callRef = callReference;
        setupCall();

        callRef.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if ((dataSnapshot.child("to").getValue()).toString().equals(myNum) && !((boolean) dataSnapshot.child("read").getValue())) {
                    listener.onNewMessage(dataSnapshot);
                    dataSnapshot.child("read").getRef().setValue(true);
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

    public void setupCall() {
        callRef.child("person1").setValue(myNum);
        callRef.child("person2").setValue(otherNum);
        callRef.child("language1").setValue(lang1);
        callRef.child("language2").setValue(lang2);
        callRef.child("finished").setValue(false);
    }

    public void setLang2(String langCode) {
        this.lang2 = langCode;
    }

    public void send_message(String message) {
        callRef.child("messages").push().setValue(new Message(message));
    }

    public void finish_conversation() {
        callRef.child("finished").setValue(true);
    }

    private class Message {
        public String text;
        public String to;
        public boolean read;

        public Message(String text) {
            this.text = text;
            this.to = otherNum;
            this.read = false;
        }
    }

    public interface OnNewMessageListener {
        void onNewMessage(DataSnapshot dataSnapshot);
    }
}
