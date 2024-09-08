package org.example.communication;

import org.example.logger.Logger;

import java.io.InputStream;

public class Reader {
    /**
     * Читає назву файлу з {@code InputStream}.
     *
     * @param inputStream Потік для читання даних.
     * @return Назва файлу.
     * @throws Exception Якщо не вдалося прочитати назву файлу.
     */
    public static String readFileName(InputStream inputStream) throws Exception {
        byte[] fileNameBytes = new byte[512];
        int fileNameLength = inputStream.read(fileNameBytes);
        if (fileNameLength == -1) {
            throw new Logger("Не вдалося прочитати назву файлу");
        }
        return new String(fileNameBytes, 0, fileNameLength).trim();
    }
}
