package org.example.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.communication.HTTP.httpConnection;

/**
 * Клас Sender надає методи для відправки JSON-даних на сервер через HTTP.
 * -file_server="localhost:9090" -manager_server="localhost:8080" -log_server="localhost:7070" -directories="Users/sirius/Desktop" -extensions=".doc,.docx,.xls,.xlsx,.ppt,.pptx" -hour=12 -minute=45 -key="a very very very very secret key" -log_file_status=false -log_manager_status=false
 */
public class Sender {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

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
            LOGGER.info("Response Code: " + responseCode);

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
