package com.example.henry.chatapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hieu on 5/1/16.
 */
public class PollingService extends IntentService {

    public static final String NAME = "PollingService";
    private String sessionId;

    public PollingService(String sessionId) {
        super(NAME);
        this.sessionId = sessionId;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            while (true) {
                try {
                    Thread.sleep(5000);
                    InputStream is = null;

                    URL url = new URL("http://10.0.2.2:8080/WebChat/api/sessions/" + sessionId + "/update");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    is = conn.getInputStream();
                    /*
                    Players players = new PlayersParser().parse(is);
                    if (!players.toString().equals(getPlayersFromDatabase().toString())) {
                        PlayersOpenHelper dbHelper = new PlayersOpenHelper(getApplicationContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.execSQL("delete from players");
                        addPlayersToDatabase(players);
                        Intent broadcastIntent = new Intent(getApplicationContext(), MyReceiver.class);
                        sendBroadcast(broadcastIntent);
                    }
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        } else {
            //textView.setText("Cannot connect!");
            Log.d(NAME, "Cannot connect! in onCreate()");
        }
    }
}
