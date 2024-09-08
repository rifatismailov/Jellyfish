package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.security.SecurityUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


class JellyfishConfig {
    private int fileServerPort;
    private String bucket;
    private String endpointMinIO;
    private String accessKey;
    private String secretKey;
    private String encryptionKey;
    private String endpointCrustaceans;

    /**
     * Конструктор для створення об'єкта JellyfishConfig.
     * Значення ключів шифруються при створенні об'єкта.
     *
     * @param fileServerPort      Порт сервера файлів.
     * @param bucket              Ім'я відра.
     * @param endpointMinIO       Точка доступу.
     * @param accessKey           Ключ доступу.
     * @param secretKey           Секретний ключ.
     * @param encryptionKey       Ключ шифрування.
     * @param endpointCrustaceans сервер для обробки файлів
     * @throws Exception Якщо виникає помилка при шифруванні.
     */
    public JellyfishConfig(int fileServerPort, String bucket, String endpointMinIO, String accessKey, String secretKey, String encryptionKey, String endpointCrustaceans) throws Exception {
        this.fileServerPort = fileServerPort;
        this.bucket = bucket;
        this.endpointMinIO = endpointMinIO;
        this.accessKey = SecurityUtils.encrypt(accessKey, "AT78TE984567356U");
        this.secretKey = SecurityUtils.encrypt(secretKey, "AT78TE984567356U");
        this.encryptionKey = SecurityUtils.encrypt(encryptionKey, "AT78TE984567356U");
        this.endpointCrustaceans = endpointCrustaceans;
    }

    /**
     * Повертає рядкове представлення об'єкта JellyfishConfig.
     *
     * @return Рядкове представлення об'єкта JellyfishConfig.
     */
    @Override
    public String toString() {
        return "ServerConfig{" +
                "fileServerPort=" + fileServerPort +
                ", bucket='" + bucket + '\'' +
                ", endpointMinIO='" + endpointMinIO + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", encryptionKey='" + encryptionKey + '\'' +
                ", endpointCrustaceans='" + endpointCrustaceans + '\'' +
                '}';
    }

    /**
     * Метод для запису об'єкта JellyfishConfig у файл у форматі JSON.
     *
     * @param config   Об'єкт JellyfishConfig, який потрібно записати.
     * @param filename Ім'я файлу, у який буде записано JSON.
     */
    public static void writeJsonToFile(JellyfishConfig config, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(config, writer); // Запис JSON у файл
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для зчитування об'єкта JellyfishConfig з файлу у форматі JSON.
     *
     * @param filename Ім'я файлу, з якого потрібно зчитати JSON.
     * @return Об'єкт JellyfishConfig, створений з даних JSON.
     */
    public static JellyfishConfig readJsonFromFile(String filename) {
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filename)) {
            // Десеріалізація JSON у об'єкт JellyfishConfig
            return gson.fromJson(reader, JellyfishConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Метод для розбору аргументів командного рядка у форматі ключ=значення.
     *
     * @param args Аргументи командного рядка.
     * @return Мапа ключ-значення, де ключі та значення отримані з аргументів.
     */
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();

        for (String arg : args) {
            String[] keyValue = arg.split("=", 2); // Розбиваємо рядок за знаком "="
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]); // Кладемо ключ і значення в мапу
            }
        }

        return params;
    }

    /**
     * Метод для створення об'єкта JellyfishConfig з аргументів командного рядка.
     *
     * @param args Аргументи командного рядка.
     * @return Об'єкт JellyfishConfig, створений з аргументів командного рядка.
     * @throws Exception Якщо виникає помилка при шифруванні.
     */
    public static JellyfishConfig parseConfig(String[] args) throws Exception {
        Map<String, String> params = parseArgs(args);

        // Парсимо значення та створюємо об'єкт JellyfishConfig
        int fileServerPort = Integer.parseInt(params.getOrDefault("fileServerPort", "9090"));
        String bucket = params.getOrDefault("bucket", "jellyfish");
        String endpointMinIO = params.getOrDefault("endpointMinIO", "http://localhost:9001");
        String accessKey = params.getOrDefault("accessKey", "admin");
        String secretKey = params.getOrDefault("secretKey", "jellyfish");
        String encryptionKey = params.getOrDefault("encryptionKey", "jellyfish");
        String endpointCrustaceans = params.getOrDefault("endpointCrustaceans", "http://localhost:8090");

        // Створюємо конфігураційний об'єкт
        return new JellyfishConfig(fileServerPort, bucket, endpointMinIO, accessKey, secretKey, encryptionKey, endpointCrustaceans);
    }

    // Геттери та сеттери для полів конфігурації

    /**
     * Повертає порт сервера файлів.
     *
     * @return Порт сервера файлів.
     */
    public int getFileServerPort() {
        return fileServerPort;
    }

    /**
     * Встановлює порт сервера файлів.
     *
     * @param fileServerPort Порт сервера файлів.
     */
    public void setFileServerPort(int fileServerPort) {
        this.fileServerPort = fileServerPort;
    }

    /**
     * Повертає ім'я відра.
     *
     * @return Ім'я відра.
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Встановлює ім'я відра.
     *
     * @param bucket Ім'я відра.
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Повертає точку доступу.
     *
     * @return Точка доступу.
     */
    public String getEndpointMinIO() {
        return endpointMinIO;
    }

    /**
     * Встановлює точку доступу.
     *
     * @param endpointMinIO Точка доступу.
     */
    public void setEndpointMinIO(String endpointMinIO) {
        this.endpointMinIO = endpointMinIO;
    }

    /**
     * Повертає ключ доступу. Дешифрує значення ключа.
     *
     * @return Дешифрований ключ доступу.
     * @throws Exception Якщо виникає помилка при дешифруванні.
     */
    public String getAccessKey() throws Exception {
        return SecurityUtils.decrypt(accessKey, "AT78TE984567356U");
    }

    /**
     * Встановлює ключ доступу. Шифрує значення ключа.
     *
     * @param accessKey Ключ доступу.
     * @throws Exception Якщо виникає помилка при шифруванні.
     */
    public void setAccessKey(String accessKey) throws Exception {
        this.accessKey = SecurityUtils.encrypt(accessKey, "AT78TE984567356U");
    }

    /**
     * Повертає секретний ключ. Дешифрує значення ключа.
     *
     * @return Дешифрований секретний ключ.
     * @throws Exception Якщо виникає помилка при дешифруванні.
     */
    public String getSecretKey() throws Exception {
        return SecurityUtils.decrypt(secretKey, "AT78TE984567356U");
    }

    /**
     * Встановлює секретний ключ. Шифрує значення ключа.
     *
     * @param secretKey Секретний ключ.
     * @throws Exception Якщо виникає помилка при шифруванні.
     */
    public void setSecretKey(String secretKey) throws Exception {
        this.secretKey = SecurityUtils.encrypt(secretKey, "AT78TE984567356U");
    }

    /**
     * Повертає ключ шифрування. Дешифрує значення ключа.
     *
     * @return Дешифрований ключ шифрування.
     * @throws Exception Якщо виникає помилка при дешифруванні.
     */
    public String getEncryptionKey() throws Exception {
        return SecurityUtils.decrypt(encryptionKey, "AT78TE984567356U");
    }

    /**
     * Встановлює ключ шифрування. Шифрує значення ключа.
     *
     * @param encryptionKey Ключ шифрування.
     * @throws Exception Якщо виникає помилка при шифруванні.
     */
    public void setEncryptionKey(String encryptionKey) throws Exception {
        this.encryptionKey = SecurityUtils.encrypt(encryptionKey, "AT78TE984567356U");
    }

    /**
     * Повертає точку доступу до сервера обробки докментів.
     *
     * @return Точка доступу до сервера обробки докментів.
     */
    public String getEndpointCrustaceans() {
        return endpointCrustaceans;
    }

    /**
     * Встановлює точку доступу до сервера обробки докментів.
     *
     * @param endpointCrustaceans Точка доступу до сервера обробки докментів.
     */
    public void setEndpointCrustaceans(String endpointCrustaceans) {
    }
}
