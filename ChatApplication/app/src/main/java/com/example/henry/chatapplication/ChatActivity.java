package com.example.henry.chatapplication;

import android.app.Activity;
import android.app.IntentService;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SharedPreferences sharedPreference;
    private final String SESSION = "SESSION";
    public static final String NAME = "ChatActivity";
    private User user;
    //Views
    EditText chatBox;
    ListView chatView;

    EntriesOpenHelper dbHelper = null;
    SQLiteDatabase db = null;
    SimpleCursorAdapter adapter;
    static final String[] PROJECTION = new String[]{"_id", "username", "message"};
    static final String SELECTION = "";

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
    }

    // Store sessionId in SharedPreferences and create new User when LoginActivity finishes
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

                // Setup the database, listview and call Service
                init();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(ChatActivity.this, PollingService.class);
        stopService(intent);
    }

    private void renderLogin() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    // Create the Database, ListView and start polling
    private void init() {
        Log.d(NAME, "Inside init()");

        chatBox = (EditText)findViewById(R.id.chat_box);
        chatView = (ListView)findViewById(R.id.chat_view);

//        dbHelper = new EntriesOpenHelper(this);
//        db = dbHelper.getWritableDatabase();

        setUpAdapter();
        chatView.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);

        // **** Start polling service **** //
        Intent intent = new Intent(ChatActivity.this, PollingService.class);
        intent.putExtra("sessionId", user.getSessionId());
        startService(intent);
        // **** //

        Button sendButton = (Button)findViewById(R.id.send_button);
        Button logoutButton = (Button)findViewById(R.id.logout_button);

        //**** Send message by executing SendMessageTask ****//
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatBox.getText().toString();
                if (!message.equals("")) {
                    chatBox.setText("");
                    new SendMessageTask().execute(message);
                }
            }
        });
        // **** //

        //**** Logout Button will remove sessionId and redirect to LoginActivity ****//
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                reset();
                renderLogin();
            }
        });
        // **** //

        // Auto join broadcast chatroom
        user.joinRoom(0);
    }

    private void setUpAdapter() {
        String[] fromColumns = {"username", "message"};
        int[] toViews = {android.R.id.text1, android.R.id.text2};
        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                null, fromColumns, toViews, 0);
    }

    // Remove sessionId
    private void reset() {
        SharedPreferences sp = getSharedPreferences(SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("sessionId");
        editor.commit();
        user.setSessionId(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, EntriesProvider.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    // Validate if the server has the sessionId
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

        // If the server still has sessionId, it will create a User. If not, redirect to LoginActivity
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

                // Calling polling service
                init();
            } catch (NullPointerException exception) {
                Log.d(NAME, "Error parsing the response");
                exception.printStackTrace();
            }
        }
    }

    // Send the message to the server by POST request
    class SendMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String message = params[0];
            try {
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/events/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");
                conn.setRequestProperty("sessionId", user.getSessionId());
                StringBuilder request = new StringBuilder();
                request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                request.append("<chatEntry>");
                    request.append("<roomId>").append(0).append("</roomId>");
                    request.append("<message>").append(message).append("</message>");
                request.append("</chatEntry>");
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(request.toString());
                os.flush();
                os.close();
                Log.d(NAME, "" + conn.getResponseCode());
                Log.d(NAME, conn.getResponseMessage());
                InputStream is = conn.getInputStream();
                Scanner scanner = new Scanner(is);
                while (scanner.hasNextLine()) {
                    Log.d(NAME, scanner.nextLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
