package org.example.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.communication.Http.httpConnection;
import static org.example.logger.Logger.message;

public class Sender {

    public static void senderJsonWithHTTP(String endpoint,Object o) {
        try {
            URL url = new URL(endpoint + "/api/" + o.getClass().getSimpleName());
            HttpURLConnection httpURLConnection = httpConnection(url);

            try (OutputStream os = httpURLConnection.getOutputStream()) {  // Відправляємо JSON через OutputStream
                byte[] input = objectToJson(o).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = httpURLConnection.getResponseCode();    // Перевіряємо відповідь сервера
            message("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String objectToJson(Object o) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();   // Конвертуємо об'єкт у JSON
        String jsonInputString = objectMapper.writeValueAsString(o);
        return jsonInputString;
    }
}