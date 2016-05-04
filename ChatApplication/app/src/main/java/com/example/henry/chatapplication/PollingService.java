package com.example.henry.chatapplication;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ListView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hieu on 5/1/16.
 */
public class PollingService extends IntentService {

    public static final String NAME = "PollingService";
    private String sessionId;

    public PollingService() {
        super(NAME);
    }

    // Check for a Response every half second
    @Override
    protected void onHandleIntent(Intent intent) {
        this.sessionId = intent.getStringExtra("sessionId");
        Log.d(NAME, "SessionID: " + sessionId);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            while (true) {
                try {
                    InputStream is = null;
                    URL url = new URL("http://10.0.3.2:8080/WebChat/api/sessions/" + sessionId + "/update");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    is = conn.getInputStream();
                    Response response = new ResponseParser().parse(is);
                    processResponse(response);
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } else {
            Log.d(NAME, "Cannot connect! in onCreate()");
        }
    }

    // If the EventEntry type is "chat" then we will put the data in the database
    private void processResponse(Response response) {
        try {
            if (response.getResult() == "failure") {
                return;
            }
            if (response.getCurrent() < response.getNewest()) {
                for (int i = response.getCurrent() + 1; i <= response.getNewest(); i ++) {
                    EventEntry entry = fetchEntry(i);
                    if (entry.getType().equals("chat")) {
                        Log.d(NAME, "inside processResponse");
                        Log.d(NAME, "message: " + entry.getMessage());
                        ContentValues values = new ContentValues();
                        values.put("type", entry.getType());
                        values.put("timeStamp", entry.getTimeStamp());
                        values.put("time", entry.getTime());
                        values.put("date", entry.getDate());
                        values.put("message", entry.getMessage());
                        values.put("roomId", entry.getRoomId());
                        values.put("username", entry.getUsername());
                        getContentResolver().insert(EntriesProvider.CONTENT_URI, values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EventEntry fetchEntry(int index) {
        try {
            Log.d(NAME, "inside fetchEntry");
            InputStream is = null;
            URL url = new URL("http://10.0.3.2:8080/WebChat/api/sessions/" + sessionId + "/" + index);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            EventEntry entry = new EventEntryParser().parse(is);
            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
