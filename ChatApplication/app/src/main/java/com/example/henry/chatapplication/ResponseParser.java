package com.example.henry.chatapplication;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hieu on 5/1/16.
 */
public class ResponseParser {
    private static final String ns = null;

    public Response parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readResponse(parser);
        } finally {
            in.close();
        }
    }

    private Response readResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Response response = new Response();

        parser.require(XmlPullParser.START_TAG, ns, "response");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("result")) {
                response.setResult(readResult(parser));
            } else if (name.equals("username")) {
                response.setUsername(readUsername(parser));
            } else if (name.equals("sessionId")) {
                response.setSessionId(readSessionId(parser));
            } else {
                skip(parser);
            }
        }
        return response;
    }

    private String readResult(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "result");
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "result");
        return result;
    }

    private String readUsername(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "username");
        String username = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "username");
        return username;
    }

    private String readSessionId(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "sessionId");
        String sessionId = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "sessionId");
        return sessionId;
    }

    private String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch(parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
