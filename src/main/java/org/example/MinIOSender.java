package org.example;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIOSender {
    public static void Send(String fileName, String filePath) {
        try {
            // Створіть клієнт MinIO
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://192.168.51.131:9001") // або вказуйте ваш MinIO сервер
                    .credentials("admin", "27Zeynalov") // Ваші креденшіали
                    .build();

            // Вказати бакет, куди завантажувати файл
            String bucketName = "example-bucket";

            // Створіть бакет, якщо він ще не існує
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            Thread.sleep(10);

            // Зберігання файлу

            InputStream fileStream = new FileInputStream(filePath);

            // Використовуємо PutObjectArgs для завантаження файлу
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(fileStream, fileStream.available(), -1) // stream, size, partSize
                            .contentType("application/octet-stream") // опціонально: тип контенту
                            .build()
            );

            System.out.println("Файл успішно завантажено до MinIO!");

        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            System.err.println("Помилка при зберіганні файлу: " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
