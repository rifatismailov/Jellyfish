package org.example;
import io.minio.*;
import io.minio.errors.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioExample {
    public static void main(String[] args) {
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
            String fileName = "1.docx";
            InputStream fileStream = new FileInputStream("received_files/1.docx");

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
