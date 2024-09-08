package org.example.security;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class SecurityUtils {
    private static final String ALGORITHM = "AES"; // Алгоритм шифрування (AES)
    private static final String TRANSFORMATION = "AES"; // Трансформація (AES)

    /**
     * Метод для шифрування тексту за допомогою алгоритму AES.
     *
     * @param input Текст, який потрібно зашифрувати.
     * @param key   Ключ для шифрування. Довжина ключа повинна відповідати вимогам AES (16, 24 або 32 символи).
     * @return Зашифрований текст у форматі Base64.
     * @throws Exception Якщо виникає помилка під час шифрування.
     */
    public static String encrypt(String input, String key) throws Exception {
        // Створюємо секретний ключ із використанням алгоритму AES
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        // Ініціалізуємо шифрування
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // Шифруємо текст
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        // Повертаємо результат у вигляді рядка, закодованого в Base64
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Метод для дешифрування тексту за допомогою алгоритму AES.
     *
     * @param input Зашифрований текст у форматі Base64.
     * @param key   Ключ для дешифрування. Ключ повинен бути таким самим, як і при шифруванні.
     * @return Дешифрований текст у вигляді рядка.
     * @throws Exception Якщо виникає помилка під час дешифрування.
     */
    public static String decrypt(String input, String key) throws Exception {
        // Створюємо секретний ключ із використанням алгоритму AES
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        // Ініціалізуємо дешифрування
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // Декодуємо зашифрований текст з формату Base64
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        // Дешифруємо текст
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        // Повертаємо результат у вигляді рядка
        return new String(decryptedBytes);
    }

    /**
     * Створює об'єкт {@link CipherInputStream}, який забезпечує розшифрування даних з входу.
     * <p>
     * Цей метод використовує алгоритм AES з режимом CFB (Cipher Feedback) і без заповнення (NoPadding).
     *
     * @param inputStream {@link InputStream} - потік, з якого буде зчитуватися зашифрований текст.
     * @param key         {@link String} - секретний ключ для дешифрування. Має бути довжиною 16 байтів (128 біт) для AES.
     * @param iv          {@link byte[]} - вектор ініціалізації (IV) для режиму CFB. Має бути довжиною 16 байтів (128 біт).
     * @return {@link CipherInputStream} - потік, який розшифровує дані, отримані з `inputStream`, використовуючи зазначений ключ і IV.
     * @throws Exception Якщо виникає помилка при ініціалізації шифра або при створенні {@link CipherInputStream}.
     */
    public static CipherInputStream getCipherInputStream(InputStream inputStream, String key, byte[] iv) throws Exception {
        // Налаштування AES розшифрування
        Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return new CipherInputStream(inputStream, cipher);
    }

    /**
     * Розшифровує і зберігає файл, перевіряючи його хеш.
     *
     * @param inputStream    Потік для читання зашифрованих даних.
     * @param filePath       Шлях для збереження файлу.
     * @param clientFileHash Хеш файлу, надісланий клієнтом.
     * @param iv             Вектор ініціалізації.
     * @return {@code true}, якщо хеші співпадають; інакше {@code false}.
     * @throws Exception Якщо виникла помилка при розшифруванні або збереженні файлу.
     */
    public static boolean decryptAndSaveFile(InputStream inputStream, String filePath, byte[] clientFileHash, byte[] iv, String KEY) throws Exception {
        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        try (CipherInputStream cipherInputStream = SecurityUtils.getCipherInputStream(inputStream, KEY, iv);
             FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                md5Digest.update(buffer, 0, bytesRead);
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        byte[] serverFileHash = md5Digest.digest();
        return Arrays.equals(clientFileHash, serverFileHash);
    }

}

