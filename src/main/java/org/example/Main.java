package org.example;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String SAVE_DIR = "received_files/";

    public static void main(String[] args) throws Exception {
        new File(SAVE_DIR).mkdirs();
        /** Executors.newCachedThreadPool(),
         *  що дозволяє створювати нові потоки при необхідності і закривати їх після завершення завдання.
         *  Цей підхід може бути ефективним, якщо у вас змінна кількість підключень.*/
        int port=Integer.parseInt(args[0]);;
        ExecutorService executorService = Executors.newCachedThreadPool();  // Використовуємо CachedThreadPool

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Сервер запущено на порту "+port+"...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Новий клієнт підключився: " + clientSocket.getInetAddress().getHostAddress());

            // Передаємо обробку підключення в пул потоків з викликом Jellyfish
            executorService.submit(() -> new Jellyfish(clientSocket).start());
        }

    }
}