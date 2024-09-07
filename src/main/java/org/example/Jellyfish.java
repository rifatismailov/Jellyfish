package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.CipherInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.Logger.*;

public class Jellyfish extends Thread {
    private final String SAVE_DIR = "received_files/";
    private final Socket client;
    private final Storage storage;
    private final String KEY;
    private String macAddress;
    private HostInfo hostInfo;

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

            // Read the filename (512 bytes)
            byte[] fileNameBytes = new byte[512];
            int fileNameLength = inputStream.read(fileNameBytes);
            if (fileNameLength == -1) {
                throw new Logger("Не вдалося прочитати назву файлу");
            }

            String fileName = new String(fileNameBytes, 0, fileNameLength).trim();
            message("Назва отриманого файлу: ", fileName);

            // Handle JSON prefix
            Pattern pattern = Pattern.compile("\\{.*\\}");
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                // Вилучаємо знайдений JSON-об'єкт
                String jsonPart = matcher.group();
                // треба буде зробити окремим потоком для теста
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(jsonPart);
                this.macAddress = root.get("MACAddress").asText();
                hostInfo = new HostInfo(
                        root.get("HostName").asText(),
                        root.get("HostAddress").asText(),
                        root.get("MACAddress").asText(),
                        root.get("RemoteAddr").asText());

                // Видаляємо JSON-об'єкт з початкового рядка
                fileName = fileName.replace(jsonPart, "");
                message("Решта рядка: ", fileName);
            } else {
                error("JSON-об'єкт не знайдено.");
            }

            String filePath = SAVE_DIR + fileName;

            // Read the file hash from the client (16 bytes for MD5)
            byte[] clientFileHash = new byte[16];
            int bytesRead = inputStream.read(clientFileHash);
            if (bytesRead != 16) {
                throw new Logger("Не вдалося прочитати хеш файлу");
            }

            // Read the IV (16 bytes for AES)
            byte[] iv = new byte[16];
            bytesRead = inputStream.read(iv);
            if (bytesRead != 16) {
                throw new Logger("Не вдалося прочитати IV");
            }

            // Decrypt and save the file while computing its hash
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            try (CipherInputStream cipherInputStream = SecurityUtils.getCipherInputStream(inputStream, KEY, iv);
                 FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                    md5Digest.update(buffer, 0, bytesRead);
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            byte[] serverFileHash = md5Digest.digest();

            // Compare hashes
            if (Arrays.equals(clientFileHash, serverFileHash)) {
                log("Отримано та збережено файл: ", fileName, " (Хеш перевірено)");
            } else {
                error("Невідповідність хешу файлу: ", fileName);
            }
            storage.Send("[" + macAddress + "]" + fileName, filePath);
            String url = Storage.getPresignedUrl(storage.getMinioClient(), storage.getBucketName(), "[" + macAddress + "]" + fileName);
            message("URL FILE ", url);
            if (client.isClosed()) {
                warning("Клієнт відключився");
            }
        } catch (Exception e) {
            error("Error handling connection: ", e.getMessage());
        }
    }
}
