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
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    public static final String NAME = "LoginActivity";
    private EditText txtUsername;
    private EditText txtPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("onCreate","run");
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);

        // **** Login Button checks username and password by executing AccountValidator **** //
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("onClick","run");
                    String username = txtUsername.getText().toString();
                    String password = txtPassword.getText().toString();

                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected()) {
                        if (!username.equals("") && !password.equals(""))
                        new AccountValidator().execute(username, password);
                    } else {
                        Log.d(NAME, "Cannot connect! in onCreate()");
                    }
                } catch (NullPointerException e) {
                    Log.d(NAME, "Empty edit text");
                }
            }
        });
        // **** **** //

        // **** Register Button redirects to RegisterActivity and LoginActivity waits for its data **** //
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), RegisterActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        // **** **** //
    }

    // When registering is successful, redirect to ChatActivity with username and sessionId
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String username = data.getStringExtra("username");
                String sessionId = data.getStringExtra("sessionId");
                Intent intent = new Intent();
                intent.putExtra("username", username);
                intent.putExtra("sessionId", sessionId);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    }

    // Check if username and password are correct
    class AccountValidator extends AsyncTask<String, Void, Response> {

        public static final String NAME = "AccountValidator";

        @Override
        protected Response doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            try {
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/sessions/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");
                StringBuilder request = new StringBuilder();
                request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                request.append("<user>");
                    request.append("<username>").append(username).append("</username>");
                    request.append("<password>").append(password).append("</password>");
                request.append("</user>");
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(request.toString());
                os.flush();
                os.close();
                Log.d(NAME, "" + conn.getResponseCode());
                Log.d(NAME, conn.getResponseMessage());
                InputStream is = conn.getInputStream();
                // receive a Response object from the server and parse it
                Response response = new ResponseParser().parse(is);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // If the result of response is success, redirect to ChatActivity
        @Override
        protected void onPostExecute(Response response) {
            try {
                if (response.getResult().equals("success")) {
                    Intent intent = new Intent();
                    intent.putExtra("username", response.getUsername());
                    intent.putExtra("sessionId", response.getSessionId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong username and password", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
