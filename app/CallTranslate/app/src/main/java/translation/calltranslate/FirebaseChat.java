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

    private Firebase chat_ref;
    private String myNum;
    private String otherNum;
    private String lang1;
    private String lang2;
    private String id;
    private boolean finished;

    public FirebaseChat(String toPhone, Context context, final OnNewMessageListener listener) {
        SharedPreferences prefs = context.getSharedPreferences("translation.calltranslate", Context.MODE_PRIVATE);
        this.myNum = prefs.getString("phoneNumber", null);
        this.otherNum = toPhone;
        setChatId();
        this.lang1 = Locale.getDefault().getLanguage();
        this.lang2 = "";
        this.finished = false;

        Firebase db = new Firebase("https://vivid-inferno-6896.firebaseio.com");
        chat_ref = db.child(id);
        chat_ref.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                if(((Long) dataSnapshot.child("to").getValue()).intValue() == my_ref && (boolean)dataSnapshot.child("read").getValue() == false) {
//                    listener.onNewMessage(dataSnapshot);
//                    dataSnapshot.child("read").getRef().setValue(false);
//                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void send_message(String text) {
//        chat_ref.child("messages").push().setValue(new Message(text, my_ref), new Firebase.CompletionListener() {
//            @Override
//            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
//
//            }
//        });
    }

    public void finish_conversation() {
        chat_ref.child("finished").setValue(true);
    }

    private void setChatId() {
        this.id = this.myNum + "_" + this.otherNum;
    }

    private class Message {
        public String text;
        public int to;
        public boolean read;

        public Message(String text, int my_ref) {
            this.text = text;
            to = (my_ref == 1 ? 1 : 0) + 1;
            read = false;
        }
    }

    public interface OnNewMessageListener {
        void onNewMessage(DataSnapshot dataSnapshot);
    }
}
