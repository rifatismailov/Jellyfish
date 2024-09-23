package org.example.communication;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Конструктор для створення об'єкта Storage.
 *
 * @param minioClient Об'єкт MinioClient для взаємодії з MinIO сервером.
 * @param bucketName  Ім'я відра, яке буде використовуватися для зберігання файлів.
 */
public record Storage(MinioClient minioClient, String bucketName) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Storage .class);

    /**
     * Створює та повертає об'єкт MinioClient для взаємодії з MinIO сервером.
     *
     * @param endpoint  URL MinIO сервера.
     * @param accessKey Ключ доступу.
     * @param secretKey Секретний ключ.
     * @return Об'єкт MinioClient.
     */
    public static MinioClient minio(String endpoint, String accessKey, String secretKey) {
        return MinioClient.builder()
                .endpoint(endpoint) // Вказуємо URL MinIO сервера
                .credentials(accessKey, secretKey) // Вказуємо ключі доступу
                .build();
    }

    /**
     * Перевіряє, чи існує відро з вказаним ім'ям. Якщо не існує, створює його.
     *
     * @param minioClient Об'єкт MinioClient для взаємодії з MinIO сервером.
     * @param bucketName  Ім'я відра, яке потрібно перевірити.
     * @return true, якщо відро існує або було створене успішно; false, якщо виникла помилка.
     */
    public static boolean bucketExists(MinioClient minioClient, String bucketName) {
        boolean isExist = false;
        try {
            // Перевіряємо, чи існує відро
            isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                // Створюємо відро, якщо не існує
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.err.println("Помилка при перевірці або створенні відра: " + e);
        }
        return isExist;
    }

    /**
     * Отримує тимчасовий URL для завантаження об'єкта з MinIO.
     *
     * @param minioClient MinioClient екземпляр.
     * @param bucketName  Ім'я бакету.
     * @param objectName  Ім'я об'єкта.
     * @return Presigned URL для завантаження об'єкта.
     */
    public static String getPresignedUrl(MinioClient minioClient, String bucketName, String objectName) {
        try {
            // Отримання presigned URL для об'єкта
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS) // URL буде дійсний протягом 1 години
                            .build()
            );
        } catch (MinioException e) {
            LOGGER.error("Помилка при отриманні presigned URL: " + e);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            LOGGER.error("Загальна помилка: " + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Завантажує файл до вказаного відра на MinIO сервер.
     *
     * @param fileName Ім'я файлу, яке буде використано в MinIO.
     * @param filePath Шлях до файлу на локальному диску.
     */
    public void Send(String fileName, String filePath) {
        try {
            InputStream fileStream = new FileInputStream(filePath);
            // Використовуємо PutObjectArgs для завантаження файлу
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(fileStream, fileStream.available(), -1) // Вказуємо InputStream, розмір файлу, розмір частини
                            .contentType("application/octet-stream") // Тип контенту (опціонально)
                            .build()
            );

            LOGGER.info("Файл успішно завантажено до MinIO!");

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            LOGGER.warn("Помилка при зберіганні файлу: " + e);
        }
    }
}
