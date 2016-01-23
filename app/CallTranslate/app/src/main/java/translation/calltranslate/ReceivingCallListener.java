package translation.calltranslate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Locale;

public class ReceivingCallListener extends Service {

    private static final String TAG = "Service";

    @Override
    public void onCreate() {
        Firebase.setAndroidContext(getApplicationContext());
        Firebase db = new Firebase("https://vivid-inferno-6896.firebaseio.com/");
        SharedPreferences prefs = getSharedPreferences("translation.calltranslate", Context.MODE_PRIVATE);
        final String my_phone = prefs.getString("phoneNumber", null);
        if(my_phone != null) {
            db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snap, String s) {
                    s = snap.getKey();
                    if (s.contains(my_phone) && (s.indexOf(my_phone) == 0 || s.indexOf(my_phone) == 10) && (snap.child("lang1").getValue() == null || snap.child("lang2").getValue() == null)) {
                        Log.d(TAG, "Detected new phone call with id: " + s);
                        if (snap.child("lang1").getValue() == null) {
                            snap.getRef().child("lang1").setValue(Locale.getDefault().getDisplayLanguage());
                        } else {
                            snap.getRef().child("lang2").setValue(Locale.getDefault().getDisplayLanguage());
                        }
                        Intent i = new Intent(getApplicationContext(), ReceivingCall.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
