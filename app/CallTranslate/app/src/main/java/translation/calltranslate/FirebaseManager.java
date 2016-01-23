package translation.calltranslate;

import com.firebase.client.Firebase;

public class FirebaseManager {

    private Firebase fb;

    public FirebaseManager() {
        fb = new Firebase("https://name.firebaseio.com");

    }
}
