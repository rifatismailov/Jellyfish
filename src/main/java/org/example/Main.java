package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.minio.MinioClient;
import org.example.communication.Storage;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.JellyfishConfig.*;
import static org.example.logger.Logger.*;

/**
 * Головний клас програми, що ініціалізує сервер і обробляє клієнтські підключення.
 * <p>
 * Цей клас відповідає за:
 * <ul>
 *     <li>Створення конфігураційного об'єкта з командного рядка.</li>
 *     <li>Серіалізацію та десеріалізацію конфігураційного об'єкта у формат JSON.</li>
 *     <li>Налаштування клієнта MinIO для роботи з об'єктним сховищем.</li>
 *     <li>Запуск серверного сокета для прийому клієнтських підключень.</li>
 *     <li>Обробку кожного підключення за допомогою пулу потоків.</li>
 * </ul>
 * <p>
 * В класі використовуються анотації для логування важливих повідомлень.
 */
public class Main {

    /**
     * Шлях до директорії, де зберігаються отримані файли.
     */
    private static final String SAVE_DIR = "received_files/";

    /**
     * Головний метод програми.
     * <p>
     * Цей метод:
     * <ul>
     *     <li>Створює директорію для збереження файлів, якщо вона ще не існує.</li>
     *     <li>Парсить конфігураційний об'єкт з аргументів командного рядка.</li>
     *     <li>Серіалізує конфігураційний об'єкт у JSON і записує його у файл.</li>
     *     <li>Читає JSON з файлу і десеріалізує його в конфігураційний об'єкт.</li>
     *     <li>Виводить зчитаний об'єкт у консоль у форматі JSON.</li>
     *     <li>Налаштовує MinIO клієнта і перевіряє існування вказаного бакету.</li>
     *     <li>Запускає сервер на вказаному порту та обробляє підключення клієнтів у фонових потоках.</li>
     * </ul>
     *
     * @param args аргументи командного рядка, що містять конфігурацію для програми
     * @throws Exception якщо виникає помилка при обробці конфігурації або запуску сервера
     */
    public static void main(String[] args) throws Exception {

        // Створюємо директорію для збереження файлів
        new File(SAVE_DIR).mkdirs();

        // Створюємо конфігураційний об'єкт з аргументів командного рядка
        JellyfishConfig config = parseConfig(args);
        // Серіалізуємо конфігураційний об'єкт у JSON і записуємо у файл
        writeJsonToFile(config, "config.json");
        if (args.length == 0) {
            // Читаємо JSON з файлу і десеріалізуємо в конфігураційний об'єкт
            config = readJsonFromFile("config.json");
        }
        // Виводимо зчитаний об'єкт у консоль
        if (config != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(config);
            message(jsonString);
        }


        // Налаштовуємо MinIO клієнта
        String bucketName = config.getBucket();
        MinioClient minioClient = Storage.minio(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());
        if (Storage.bucketExists(minioClient, bucketName)) {
            log("Bucket з назвою ", bucketName, " вже існує");
        }

        Storage storage = new Storage(minioClient, bucketName);

        // Створюємо пул потоків для обробки клієнтських підключень
        int port = Integer.parseInt(String.valueOf(config.getFileServerPort()));
        ExecutorService executorService = Executors.newCachedThreadPool(); // Використовуємо CachedThreadPool

        ServerSocket serverSocket = new ServerSocket(port);
        message("Сервер запущено на порту " + port + "...");

        // Основний цикл для обробки клієнтських підключень
        while (true) {
            Socket clientSocket = serverSocket.accept();
            message("Новий клієнт підключився: ", clientSocket.getInetAddress().getHostAddress());

            // Обробляємо підключення клієнта у фоновому потоці
            JellyfishConfig finalConfig = config;
            executorService.submit(() -> {
                try {
                    new Jellyfish(clientSocket, storage, finalConfig.getEncryptionKey()).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}

/**
 * Щодо використання ExecutorService та CachedThreadPool:
 * <p>
 * {@code Executors.newCachedThreadPool()} створює пул потоків, який може динамічно збільшуватися і зменшуватися залежно від навантаження.
 * Якщо пул потоків не має вільних потоків, він створює нові потоки за потреби.
 * Якщо потоки не використовуються протягом певного часу (під час бездіяльності), вони можуть бути видалені.
 * <p>
 * CachedThreadPool автоматично управляє створенням і видаленням потоків.
 * Якщо потік не використовується протягом певного часу, він буде закритий і видалений з пулу.
 * Коли клієнт відключається, потік завершує свою роботу.
 * Якщо потік більше не потрібен (через закриття клієнта), він повертається в пул або видаляється.
 */
