package translation.calltranslate;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CallActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private String microsoftAuthUrl = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        try {
            getTranslation1(new Callback() {
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
                            System.out.println("Access token: " + accessToken);

                            String uri = Uri.parse("http://api.microsofttranslator.com/v2/Http.svc/Translate?")
                                    .buildUpon()
                                    .appendQueryParameter("text", "How are you doing?")
                                    .appendQueryParameter("from", "en")
                                    .appendQueryParameter("to", "ar")
                                    .build().toString();

                            String header = "Bearer " + accessToken;

                            try {
                                getTranslation2(uri, header);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Request not successful
                        throw new IOException("Unexpected code " + response);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Call getTranslation1(Callback callback) throws Exception {
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
        call.enqueue(callback);
        return call;
    }

    private void getTranslation2(String url, String header) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", header)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                System.out.println(response.body().string());
            }
        });
    }
}
