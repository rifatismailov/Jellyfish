package org.example.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.communication.Http.httpConnection;
import static org.example.logger.JLogger.info;

/**
 * Клас Sender надає методи для відправки JSON-даних на сервер через HTTP.
 */
public class Sender {

    /**
     * Відправляє об'єкт у форматі JSON на вказаний endpoint через HTTP POST запит.
     *
     * @param endpoint URL-адреса сервера, на який буде відправлено запит.
     * @param o        Об'єкт, який потрібно відправити. Клас об'єкта буде використано для формування частини URL.
     */
    public static void senderJsonWithHTTP(String endpoint, Object o) {
        try {
            // Формування URL для запиту
            URL url = new URL(endpoint + "/api/" + o.getClass().getSimpleName());
            HttpURLConnection httpURLConnection = httpConnection(url);

            // Відправка JSON через OutputStream
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = objectToJson(o).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Отримання та логування коду відповіді сервера
            int responseCode = httpURLConnection.getResponseCode();
            info("Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Конвертує об'єкт у формат JSON.
     *
     * @param o Об'єкт, який потрібно конвертувати у JSON.
     * @return JSON-рядок, що представляє об'єкт.
     * @throws JsonProcessingException Якщо сталася помилка під час перетворення об'єкта у JSON.
     */
    private static String objectToJson(Object o) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();   // Ініціалізація ObjectMapper для конвертації об'єкта у JSON
        String jsonInputString = objectMapper.writeValueAsString(o);
        return jsonInputString;
    }
}
