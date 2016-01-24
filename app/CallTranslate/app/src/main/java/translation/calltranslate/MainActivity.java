package translation.calltranslate;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends AppCompatActivity {

    private int CALL_REQ_CODE = 200;

    private Context context;
    private SharedPreferences prefs;
    private int SETUP_ACTIVITY_REQ_CODE = 100;
    private ArrayList<SelectUser> selectUsers;
    private List<SelectUser> temp;
    private ListView listView;
    private Cursor phones, email;
    private ContentResolver resolver;
    private SearchView search;
    private SelectUserAdapter adapter;

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
            ab.setTitle("Conversations");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addChatIntent = new Intent(context, AddChatActivity.class);
                startActivity(addChatIntent);
            }
        });

        selectUsers = new ArrayList<SelectUser>();
        resolver = this.getContentResolver();
        listView = (ListView) findViewById(R.id.contacts_list);

        phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        LoadContact loadContact = new LoadContact();
        loadContact.execute();
    }

    private void launchPhoneRequest() {
        Intent getPhoneNumberIntent = new Intent(context, SetupActivity.class);
        startActivityForResult(getPhoneNumberIntent, SETUP_ACTIVITY_REQ_CODE);
    }

    private void getMyChats() {
        // TODO: Search firebase using my phone number for all my chats, then add them to the
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

    class LoadContact extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get Contact list from Phone
            if (phones != null) {
                Log.e("count", "" + phones.getCount());
                if (phones.getCount() == 0) {
                    Toast.makeText(MainActivity.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
                }

                while (phones.moveToNext()) {
                    Bitmap bit_thumb = null;
                    String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String image_thumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                    try {
                        if (image_thumb != null) {
                            bit_thumb = MediaStore.Images.Media.getBitmap(resolver, Uri.parse(image_thumb));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SelectUser selectUser = new SelectUser();
                    selectUser.setThumb(bit_thumb);
                    selectUser.setName(name);
                    selectUser.setPhone(phoneNumber);
                    selectUser.setCheckedBox(false);
                    selectUsers.add(selectUser);
                }
            }
            //phones.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new SelectUserAdapter(selectUsers, MainActivity.this);
            listView.setAdapter(adapter);
            // Select item on listclick
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    SelectUser data = selectUsers.get(i);
                    Intent callIntent = new Intent(context, CallActivity.class);
                    callIntent.putExtra("user", 1);
                    callIntent.putExtra("otherNumber", data.phone);
                    startActivityForResult(callIntent, CALL_REQ_CODE);
                }
            });
            listView.setFastScrollEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        phones.close();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        //WHY THE FUCK SEARCH VIEW IS NUUUUUUULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
        return true;
    }
}
