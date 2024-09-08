package org.example.communication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    public static HttpURLConnection httpConnection(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);
        return httpURLConnection;
    }
}
