package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.communication.Sender;
import org.example.communication.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.InputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.communication.Reader.readFileName;
import static org.example.security.SecurityUtils.decryptAndSaveFile;

/**
 * Клас {@code Jellyfish} представляє собою потік, який обробляє з'єднання з клієнтом,
 * отримує зашифрований файл, розшифровує його, перевіряє хеш-функцію та зберігає файл на диск.
 * Також цей клас відповідає за обробку метаданих файлу, отриманих у JSON-форматі, і надсилання
 * файл на S3 (MinIO) сервер з використанням {@code Storage} а також надсилання інформації на сервер Crustaceans для опробки файлу
 */
public class Jellyfish extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jellyfish .class);

    private static final String SAVE_DIR = "received_files/";
    private final Socket client;
    private final Storage storage;
    private final String KEY;
    private final String crustaceans;
    private String macAddress;
    private HostInfo hostInfo;

    /**
     * Конструктор класу {@code Jellyfish}.
     *
     * @param client  Socket об'єкт для з'єднання з клієнтом.
     * @param storage Об'єкт {@code Storage} для збереження файлів і отримання URL.
     * @param KEY     Ключ для розшифрування файлів.
     */
    public Jellyfish(Socket client, Storage storage, String KEY, String crustaceans) {
        this.client = client;
        this.storage = storage;
        this.KEY = KEY;
        this.crustaceans = crustaceans;
    }

    @Override
    public void run() {
        try (Socket socket = client) {
            LOGGER.info("Підключення встановлено");
            InputStream inputStream = socket.getInputStream();

            // Читання назви файлу
            String fileName = readFileName(inputStream);
            LOGGER.info("Назва отриманого файлу: " + fileName);

            // Обробка JSON-префікса
            Pattern pattern = Pattern.compile("\\{.*\\}");
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                String jsonPart = matcher.group();
                parseJson(jsonPart);
                fileName = fileName.replace(jsonPart, "");
                LOGGER.info("Решта рядка: " + fileName);
            } else {
                LOGGER.error("JSON-об'єкт не знайдено.");
            }

            String filePath = SAVE_DIR + fileName;

            byte[] clientFileHash = new byte[16];// Читання хешу файлу з клієнта (16 байт для MD5)
            int bytesRead = inputStream.read(clientFileHash);
            if (bytesRead != 16) {
                LOGGER.warn("Не вдалося прочитати хеш файлу");
            }

            byte[] iv = new byte[16];// Читання IV (16 байт для AES)
            bytesRead = inputStream.read(iv);
            if (bytesRead != 16) {
                LOGGER.warn("Не вдалося прочитати IV");
            }

            // Розшифрування і збереження файлу, перевірка хешу
            if (decryptAndSaveFile(inputStream, filePath, clientFileHash, iv, KEY)) {
                LOGGER.info("Отримано та збережено файл: " + fileName + " (Хеш перевірено)");
            } else {
                LOGGER.warn("Невідповідність хешу файлу: " + fileName);
            }

            handleFileOnServer(fileName);    // Обробка файлу на сервері

        } catch (Exception e) {
            LOGGER.warn("Помилка при обробці з'єднання: "+e);
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
     */
    private void handleFileOnServer(String fileName) {
        storage.Send("[" + macAddress + "]" + fileName, SAVE_DIR + fileName);
       // String url = Storage.getPresignedUrl(storage.minioClient(), storage.bucketName(), "[" + macAddress + "]" + fileName);
        hostInfo.setUrlFile("[" + macAddress + "]" + fileName);
        Sender.senderJsonWithHTTP(crustaceans, hostInfo);
        LOGGER.info("URL FILE " + "[" + macAddress + "]" + fileName);
        if (client.isClosed()) {
            LOGGER.warn("Клієнт відключився");
        }
    }
}
