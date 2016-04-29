package com.example.henry.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ChatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreference;
    private final String SESSION = "SESSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //**** Check session ****//
        sharedPreference = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        String session = sharedPreference.getString(SESSION, null);
        if (session == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
