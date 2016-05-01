package com.example.henry.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreference;
    private final String SESSION = "SESSION";
    private static String NAME = "ChatActivity";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            //**** Check session ****//
            sharedPreference = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
            String sessionId = sharedPreference.getString(SESSION, null);
            if (sessionId == null) {
                renderLogin();
            } else {
                new SessionValidator(this, sessionId).execute();
            }
        } else {
            //textView.setText("Cannot connect!");
            Log.d(NAME, "Cannot connect! in onCreate()");
        }

    }

    private void renderLogin() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void init() {

    }

    class SessionValidator extends AsyncTask<Void, Void, Response> {

        public final String NAME = "SessionValidator";
        private Context context;
        private String sessionId;

        public SessionValidator(Context context, String sessionId) {
            this.context = context;
            this.sessionId = sessionId;
        }

        @Override
        protected Response doInBackground(Void... params) {
            InputStream is = null;
            try {
                URL url = new URL("http://10.0.2.2:8080/WebChat/api/sessions/" + sessionId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                Response response = new ResponseParser().parse(is);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Response response) {
            try {
                if (response.getResult().equals("failure")) {
                    renderLogin();
                    return;
                }
                // The response is success now
                
            } catch (NullPointerException exception) {
                Log.d(NAME, "Error parsing the response");
                exception.printStackTrace();
            }
        }
    }
}
