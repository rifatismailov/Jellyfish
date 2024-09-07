package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.InputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.example.Logger.*;
import static org.example.Reader.readFileName;
import static org.example.SecurityUtils.decryptAndSaveFile;

/**
 * Клас {@code Jellyfish} представляє собою потік, який обробляє з'єднання з клієнтом,
 * отримує зашифрований файл, розшифровує його, перевіряє хеш-функцію та зберігає файл на диск.
 * Також цей клас відповідає за обробку метаданих файлу, отриманих у JSON-форматі, і надсилання
 * файл на S3 (MinIO) сервер з використанням {@code Storage} а також надсилання інформації на сервер Crustaceans для опробки файлу
 */
public class Jellyfish extends Thread {
    private static final String SAVE_DIR = "received_files/";
    private final Socket client;
    private final Storage storage;
    private final String KEY;
    private String macAddress;
    private HostInfo hostInfo;

    /**
     * Конструктор класу {@code Jellyfish}.
     *
     * @param client  Socket об'єкт для з'єднання з клієнтом.
     * @param storage Об'єкт {@code Storage} для збереження файлів і отримання URL.
     * @param KEY     Ключ для розшифрування файлів.
     */
    public Jellyfish(Socket client, Storage storage, String KEY) {
        this.client = client;
        this.storage = storage;
        this.KEY = KEY;
    }

    @Override
    public void run() {
        try (Socket socket = client) {
            log("Підключення встановлено");
            InputStream inputStream = socket.getInputStream();

            // Читання назви файлу
            String fileName = readFileName(inputStream);
            message("Назва отриманого файлу: ", fileName);

            // Обробка JSON-префікса
            Pattern pattern = Pattern.compile("\\{.*\\}");
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                // Вилучаємо знайдений JSON-об'єкт
                String jsonPart = matcher.group();
                parseJson(jsonPart);
                // Видаляємо JSON-об'єкт з початкового рядка
                fileName = fileName.replace(jsonPart, "");
                message("Решта рядка: ", fileName);
            } else {
                error("JSON-об'єкт не знайдено.");
            }

            String filePath = SAVE_DIR + fileName;

            // Читання хешу файлу з клієнта (16 байт для MD5)
            byte[] clientFileHash = new byte[16];
            int bytesRead = inputStream.read(clientFileHash);
            if (bytesRead != 16) {
                throw new Logger("Не вдалося прочитати хеш файлу");
            }

            // Читання IV (16 байт для AES)
            byte[] iv = new byte[16];
            bytesRead = inputStream.read(iv);
            if (bytesRead != 16) {
                throw new Logger("Не вдалося прочитати IV");
            }

            // Розшифрування і збереження файлу, перевірка хешу
            if (decryptAndSaveFile(inputStream, filePath, clientFileHash, iv,KEY)) {
                log("Отримано та збережено файл: ", fileName, " (Хеш перевірено)");
            } else {
                error("Невідповідність хешу файлу: ", fileName);
            }

            // Обробка файлу на сервері
            handleFileOnServer(fileName);

        } catch (Exception e) {
            error("Помилка при обробці з'єднання: ", e.getMessage());
        }
    }

    /**
     * Парсить JSON-об'єкт і оновлює інформацію про хост.
     *
     * @param jsonPart JSON-об'єкт.
     * @throws Exception Якщо виникла помилка при обробці JSON.
     */
    private void parseJson(String jsonPart) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonPart);
        this.macAddress = root.get("MACAddress").asText();
        hostInfo = new HostInfo(
                root.get("HostName").asText(),
                root.get("HostAddress").asText(),
                root.get("MACAddress").asText(),
                root.get("RemoteAddr").asText());
    }

    /**
     * Обробляє файл на сервері, надсилаючи його інформацію і URL.
     *
     * @param fileName Назва файлу.
     * @throws Exception Якщо виникла помилка при обробці файлу на сервері.
     */
    private void handleFileOnServer(String fileName) throws Exception {
        storage.Send("[" + macAddress + "]" + fileName, SAVE_DIR + fileName);
        String url = Storage.getPresignedUrl(storage.getMinioClient(), storage.getBucketName(), "[" + macAddress + "]" + fileName);
        hostInfo.setUrlFile(url);
        Sender.senderJson(hostInfo);
        message("URL FILE ", url);
        if (client.isClosed()) {
            warning("Клієнт відключився");
        }
    }
}
