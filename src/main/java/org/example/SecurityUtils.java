package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SecurityUtils {
    private static final String ALGORITHM = "AES"; // Алгоритм шифрування (AES)
    private static final String TRANSFORMATION = "AES"; // Трансформація (AES)

    /**
     * Метод для шифрування тексту за допомогою алгоритму AES.
     *
     * @param input Текст, який потрібно зашифрувати.
     * @param key Ключ для шифрування. Довжина ключа повинна відповідати вимогам AES (16, 24 або 32 символи).
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
     * @param key Ключ для дешифрування. Ключ повинен бути таким самим, як і при шифруванні.
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
}

