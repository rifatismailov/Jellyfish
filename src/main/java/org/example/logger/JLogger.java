package org.example.logger;

import org.example.Color;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Клас Logger забезпечує функціональність для логування повідомлень різних типів.
 */
public class JLogger extends Exception {

    private static final String LOG_FILE = "application.log"; // Шлях до файлу для збереження логів
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public JLogger(String message) {
        log("WARN", message);
    }

    /**
     * Логує інформаційне повідомлення в консоль і файл.
     * @param message Повідомлення для логування.
     */
    public static void info(String message) {
        log("INFO", message);
    }

    /**
     * Логує повідомлення про попередження в консоль і файл.
     * @param message Повідомлення про попередження для логування.
     */
    public static void warn(String message) {
        log("WARN", message);
    }
    /**
     * Логує повідомлення про помилку в консоль і файл.
     * @param message Повідомлення про помилку для логування.
     */
    public static void error(String message) {
        log("ERROR", message );
    }
    /**
     * Логує повідомлення про помилку в консоль і файл.
     * @param message Повідомлення про помилку для логування.
     * @param exception Виключення, яке сталося (може бути null).
     */
    public static void error(String message, Exception exception) {
        log("ERROR", message + (exception != null ? ": " + exception.getMessage() : ""));
    }

    /**
     * Логує повідомлення з зазначеним рівнем логування.
     * @param level Рівень логування (INFO, WARN, ERROR).
     * @param message Повідомлення для логування.
     */
    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logMessage = String.format("%s [%s] %s", timestamp, level, message);

        // Виводимо в консоль
        System.out.println(Color.BLUE.getCode() + message);

        // Записуємо в файл
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Не вдалося записати лог у файл: " + e.getMessage());
        }
    }
}

