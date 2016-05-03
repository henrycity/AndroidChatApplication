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
                        new RegisterUser().execute(username, password);
                }

            }
        });
    }

    private void renderChat() {
        Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    class RegisterUser extends AsyncTask<String, Void, Response> {


        @Override
        protected Response doInBackground(String... params) {

            try {
                String username = params[0];
                String password = params[1];
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/users/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response response) {

            try {
                if (response.getResult().equals("success")) {
                    Intent intent = new Intent(RegisterActivity.this, ChatActivity.class);
                    intent.putExtra("username", response.getUsername());
                    intent.putExtra("sessionId", response.getSessionId());
                    startActivity(intent);
                }
            } catch(Exception e) {

            }
        }
    }
}
