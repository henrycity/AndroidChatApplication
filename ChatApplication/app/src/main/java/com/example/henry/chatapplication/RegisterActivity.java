package com.example.henry.chatapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    EditText txtUsername;
    EditText txtPassword;
    Button btnLogin;
    Button btnRegister;
    String username;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = txtUsername.getText().toString();
                password = txtPassword.getText().toString();
                if (username != null && password != null) {
                        new RegisterUser(username, password).execute();
                }

            }
        });
    }

    private void renderChat() {
        Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    class RegisterUser extends AsyncTask<Void, Void, Void> {

        private String username;
        private String password;
        HttpURLConnection conn;
        HttpURLConnection connection;

        public RegisterUser(String username, String password) {
            this.username = username;
            this.password = password;
        }


        @Override
        protected Void doInBackground(Void... params) {

            try {
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/users/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");

                String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<user>" +
                        "<username>" + username + "</username>" +
                        "<password>" + password + "</password>" +
                        "</user>";
                OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                output.write(body.getBytes());
                output.flush();
                output.close();
                Log.d("background bef", "can you get here");
                InputStream is = conn.getInputStream();
                Response response = new ResponseParser().parse(is);
                Log.d("background", "can you get here");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            InputStream stream = null;
            try {
//                URL url = new URL("http://10.0.3.2:8080/WebChat/api/users/");
//                connection = (HttpURLConnection) url.openConnection();
//                connection.setReadTimeout(10000);
//                connection.setConnectTimeout(15000);
//                connection.setRequestMethod("GET");
//                connection.setDoInput(true);
                Log.d("onPostExecute", "can you get here");

                Log.d("onPostExecute", "no can you get here");

//                Response response = new ResponseParser().parse(connection.getInputStream());
//                Log.d("onPostExecute", "no can you get here");
//                Log.d("result", response.getResult());
//                if (response.getResult().equals("success")) {
//                    renderChat();
//                }
            } catch(Exception e) {

            }

        }
    }
}
