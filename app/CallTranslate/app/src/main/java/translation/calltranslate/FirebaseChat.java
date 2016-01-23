package translation.calltranslate;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;

public class FirebaseChat {

    private static final String TAG = "Firebase";

    private Firebase chat_ref;
    private Chat chat;
    private int my_ref;

    public FirebaseChat(String to_phone, Context context, ValueEventListener listener) {
        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String my_phone = "111111"; //tMgr.getLine1Number(); TODO check this
        chat = new Chat(my_phone, to_phone);
        if(chat.id != null) {
            if(chat.id.indexOf(my_phone) == 0) {
                my_ref = 1;
            } else {
                my_ref = 2;
            }
            Firebase db = new Firebase("https://scorching-torch-8125.firebaseio.com");
            chat_ref = db.child(chat.id);
            chat_ref.setValue(chat);
            chat_ref.child("messages").addValueEventListener(listener);
        }
    }

    public void send_message(String text) {
        chat_ref.push().setValue(new Message(text, my_ref), new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {

            }
        });
    }

    public void finish_conversation() {
        chat_ref.child("finished").setValue(true);
    }

    private class Chat {
        public String id;
        public String lang1;
        public String lang2;
        public boolean finished;

        public Chat(String from_num, String to_num) {
            finished = false;
            int compare = to_num.compareTo(from_num);
            if(compare < 0) {
                this.id = to_num + from_num;
            } else if(compare > 0){
                this.id = from_num + to_num;
            } else {
                Log.e(TAG, "WRONG CALLING NUMBER!");
            }
            if(id != null) {
                if(my_ref == 1) {
                    lang1 = Locale.getDefault().getDisplayLanguage();
                } else {
                    lang2 = Locale.getDefault().getDisplayLanguage();
                }
            }
        }
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
}
