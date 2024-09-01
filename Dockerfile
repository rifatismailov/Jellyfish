# Використовуємо базовий образ з OpenJDK 11
FROM openjdk:17

# Встановлюємо робочу директорію всередині контейнера
WORKDIR /app/

# Копіюємо JAR файл до робочої директорії контейнера
COPY Jellyfish-1.0-SNAPSHOT.jar /app/Jellyfish-1.0-SNAPSHOT.jar

# Вказуємо команду для запуску JAR файлу
CMD ["java", "-jar", "/app/Jellyfish-1.0-SNAPSHOT.jar"]
