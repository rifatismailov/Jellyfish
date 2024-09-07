package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.minio.MinioClient;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.JellyfishConfig.*;


public class Main {

    private static final String SAVE_DIR = "received_files/";

    public static void main(String[] args) throws Exception {

        new File(SAVE_DIR).mkdirs();
        // Створюємо конфігураційний об'єкт
        JellyfishConfig config = parseConfig(args);
        // Серіалізація об'єкта у JSON і запис у файл
        writeJsonToFile(config, "config.json");
        // Читання JSON з файлу і десеріалізація в об'єкт
        JellyfishConfig readConfig = readJsonFromFile("config.json");

        // Виведення зчитаного об'єкта
        if (readConfig != null) {
            // Серіалізація об'єкта в JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(readConfig);
            System.out.println(Color.GREEN.getCode() + jsonString);
        }
        String bucketName = config.getBucket();
        MinioClient minioClient = Storage.minio(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());
        if (Storage.bucketExists(minioClient, bucketName)) {
            System.out.println("Bucket з назвою " + bucketName + " вже існує");
        }
        Storage storage = new Storage(minioClient, bucketName);
        /** Executors.newCachedThreadPool(),
         *  що дозволяє створювати нові потоки при необхідності і закривати їх після завершення завдання.
         *  Цей підхід може бути ефективним, якщо у вас змінна кількість підключень.*/
        int port = Integer.parseInt(String.valueOf(config.getFileServerPort()));
        ExecutorService executorService = Executors.newCachedThreadPool();  // Використовуємо CachedThreadPool

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущено на порту " + port + "...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Новий клієнт підключився: " + clientSocket.getInetAddress().getHostAddress());

            // Передаємо обробку підключення в пул потоків з викликом Jellyfish
            executorService.submit(() -> {
                try {
                    new Jellyfish(clientSocket, storage, config.getEncryptionKey()).start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }
}
/**
 *  ExecutorService та CachedThreadPool:
 *  Executors.newCachedThreadPool() створює пул потоків, який може динамічно збільшуватися і зменшуватися залежно від навантаження.
 *  Якщо пул потоків не має вільних потоків, він створює нові потоки за потреби.
 *  Якщо потоки не використовуються протягом деякого часу (під час бездіяльності), вони можуть бути видалені.
 * <p>
 *  CachedThreadPool автоматично управляє створенням і видаленням потоків.
 *  Якщо поток не використовується протягом певного часу, він буде закритий і видалений з пулу.
 *  Коли клієнт відключається, потік завершує свою роботу.
 *  Якщо потік більше не потрібен (через закриття клієнта), він повертається в пул або видаляється.
 */