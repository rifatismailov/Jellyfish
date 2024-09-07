package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.Logger.message;

public class Sender {

    public static void senderJson(HostInfo hostInfo) {
        try {
            // Створюємо URL-з'єднання
            URL url = new URL("http://localhost:8090/api/hostinfo");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // Налаштовуємо запит
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setDoOutput(true);

            // Конвертуємо об'єкт у JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonInputString = objectMapper.writeValueAsString(hostInfo);

            // Відправляємо JSON через OutputStream
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Перевіряємо відповідь сервера
            int responseCode = httpURLConnection.getResponseCode();
            message("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
