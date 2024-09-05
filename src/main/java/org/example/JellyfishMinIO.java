package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JellyfishMinIO extends Thread {
    private static final String SAVE_DIR = "received_files/";
    private static final String KEY = "a very very very very secret key"; // 32-byte key for AES-256

    Socket client;

    public JellyfishMinIO(Socket client) {
        this.client = client;
    }

    private String allName;

    @Override
    public void run() {

        try (Socket socket = client) {
            System.out.println("Connection established");

            InputStream inputStream = socket.getInputStream();

            // Read the filename (256 bytes)
            byte[] fileNameBytes = new byte[512];
            int fileNameLength = inputStream.read(fileNameBytes);
            if (fileNameLength == -1) {
                throw new Exception("Failed to read file name");
            }

            String fileName = new String(fileNameBytes, 0, fileNameLength).trim();
            System.out.println("Received file name: " + fileName);

            // Handle JSON prefix
            Pattern pattern = Pattern.compile("\\{.*\\}");
            Matcher matcher = pattern.matcher(fileName);

            if (matcher.find()) {
                // Вилучаємо знайдений JSON-об'єкт
                String jsonPart = matcher.group();
                // треба буде зробити окремим потоком для теста
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(jsonPart);

                String hostName = root.get("HostName").asText();
                String hostAddress = root.get("HostAddress").asText();
                String macAddress = root.get("MACAddress").asText();
                String remoteAddr = root.get("RemoteAddr").asText();
                allName = macAddress;
                System.out.println("HostName: " + hostName);
                System.out.println("HostAddress: " + hostAddress);
                System.out.println("MACAddress: " + macAddress);
                System.out.println("RemoteAddr: " + remoteAddr);
                // до сюди

                // Видаляємо JSON-об'єкт з початкового рядка
                fileName = fileName.replace(jsonPart, "");

                // Виводимо результати
                System.out.println("JSON частина: " + jsonPart);
                System.out.println("Решта рядка: " + fileName);
            } else {
                System.out.println("JSON-об'єкт не знайдено.");
            }

            String filePath = SAVE_DIR + fileName;

            // Read the file hash from the client (16 bytes for MD5)
            byte[] clientFileHash = new byte[16];
            int bytesRead = inputStream.read(clientFileHash);
            if (bytesRead != 16) {
                throw new Exception("Failed to read file hash");
            }

            // Read the IV (16 bytes for AES)
            byte[] iv = new byte[16];
            bytesRead = inputStream.read(iv);
            if (bytesRead != 16) {
                throw new Exception("Failed to read IV");
            }

            // Set up AES decryption
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Decrypt and save the file while computing its hash
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            try (CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
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
                System.out.println("Received and saved file: " + fileName + " (Hash verified)");
            } else {
                System.out.println("Hash mismatch for file: " + fileName);
            }
            MinIOSender.Send("[" + allName + "]" + fileName, filePath);

            if (client.isClosed()) {
                System.out.println("Клієнт відключився");
            }
        } catch (Exception e) {
            System.out.println("Error handling connection: " + e.getMessage());
        }
    }
}
