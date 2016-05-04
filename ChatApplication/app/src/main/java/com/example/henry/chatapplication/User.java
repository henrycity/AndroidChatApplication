package com.example.henry.chatapplication;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hieu on 5/1/16.
 */
public class User {

    private String username;
    private String sessionId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void joinRoom(int roomId) {
        new JoinRoomTask().execute(roomId);
    }

    class JoinRoomTask extends AsyncTask<Integer, Void, Response> {

        public static final String NAME = "JoinRoomTask";

        @Override
        protected Response doInBackground(Integer... params) {
            int roomId = params[0];
            try {
                Log.d(NAME, "SessionID: " + sessionId);
                URL url = new URL("http://10.0.3.2:8080/WebChat/api/sessions/" + sessionId + "/rooms");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");
                StringBuilder request = new StringBuilder();
                request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                request.append("<chatRoom>");
                    request.append("<id>").append(roomId).append("</id>");
                request.append("</chatRoom>");
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(request.toString());
                os.flush();
                os.close();
                Log.d(NAME, "" + conn.getResponseCode());
                Log.d(NAME, conn.getResponseMessage());
                InputStream is = conn.getInputStream();
                Response response = new ResponseParser().parse(is);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Response response) {
            try {
                Log.d(NAME, response.getResult());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
