package org.example.communication;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Клас Http надає метод для налаштування та отримання HTTP-з'єднання.
 */
public class HTTP {

    /**
     * Налаштовує та повертає HTTP-з'єднання для вказаного URL.
     *
     * @param url URL-адреса, на яку потрібно налаштувати з'єднання.
     * @return Налаштоване HTTP-з'єднання.
     * @throws IOException Якщо сталася помилка під час відкриття з'єднання.
     */
    public static HttpURLConnection httpConnection(URL url) throws IOException {
        // Відкриття HTTP-з'єднання
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        // Налаштування методів запиту та заголовків
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);

        return httpURLConnection;
    }
}
