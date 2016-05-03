package com.example.henry.chatapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreference;
    private final String SESSION = "SESSION";
    public static final String NAME = "ChatActivity";
    private User user;
    //Views
    EditText chatBox;
    Button logoutButton;

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
            String sessionId = sharedPreference.getString("sessionId", null);
            Log.d(NAME, "sessionId: " + sessionId);
            if (sessionId == null) {
                renderLogin();
            } else {
                new SessionValidator().execute(sessionId);
            }
        } else {
            Log.d(NAME, "Cannot connect! in onCreate()");
        }

        logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString("sessionId", null);
                editor.commit();
                renderLogin();
            }
        });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String username = data.getStringExtra("username");
                String sessionId = data.getStringExtra("sessionId");
                SharedPreferences sharedPreference = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreference.edit();
                editor.putString("sessionId", sessionId);
                editor.commit();
                user = new User();
                user.setUsername(username);
                user.setSessionId(sessionId);
                Log.d(NAME, "sessionId inside onActivityResult: " + sharedPreference.getString("sessionId", null));

                init();
            }
        }
    }

    private void renderLogin() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void init() {
        Log.d(NAME, "Inside init()");
        chatBox = (EditText)findViewById(R.id.chat_box);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatBox.getText().toString();
                chatBox.setText("");
            }
        });

        user.joinRoom(0);
    }

    class SessionValidator extends AsyncTask<String, Void, Response> {

        public static final String NAME = "SessionValidator";

        @Override
        protected Response doInBackground(String... params) {
            InputStream is = null;
            String sessionId = params[0];
            try {
                Log.d(NAME, "SessionID: " + sessionId);
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/sessions/" + sessionId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                Response response = new ResponseParser().parse(is);
                response.setSessionId(sessionId);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response response) {
            try {
                if (response.getResult().equals("failure")) {
                    renderLogin();
                    return;
                }
                // The response is success now
                user = new User();
                user.setUsername(response.getUsername());
                user.setSessionId(response.getSessionId());

                init();
            } catch (NullPointerException exception) {
                Log.d(NAME, "Error parsing the response");
                exception.printStackTrace();
            }
        }
    }
}
