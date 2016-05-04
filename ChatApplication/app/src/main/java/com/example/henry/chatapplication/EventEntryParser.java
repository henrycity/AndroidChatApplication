package com.example.henry.chatapplication;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hieu on 5/3/16.
 */
public class EventEntryParser {
    private static final String ns = null;

    public EventEntry parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readEventEntry(parser);
        } finally {
            in.close();
        }
    }

    private EventEntry readEventEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        EventEntry entry = new EventEntry();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("type")) {
                entry.setType(readString(parser, "type"));
            } else if (name.equals("timeStamp")) {
                entry.setTimeStamp(readTimeStamp(parser));
            } else if (name.equals("time")) {
                entry.setTime(readString(parser, "time"));
            } else if (name.equals("date")) {
                entry.setDate(readString(parser, "date"));
            } else if (name.equals("message")){
                entry.setMessage(readString(parser, "message"));
            } else if (name.equals("roomId")){
                entry.setRoomId(readRoomId(parser));
            } else if (name.equals("username")){
                entry.setUsername(readString(parser, "username"));
            } else {
                skip(parser);
            }
        }
        return entry;
    }

    private String readString(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    private long readTimeStamp(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "timeStamp");
        String timeStamp = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "timeStamp");
        return Long.parseLong(timeStamp);
    }

    private int readRoomId(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "roomId");
        String roomId = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "roomId");
        return Integer.parseInt(roomId);
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
