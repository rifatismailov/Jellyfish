package org.example.communication;



import org.example.Jellyfish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.InputStream;

public class Reader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Reader .class);

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
            LOGGER.warn("Не вдалося прочитати назву файлу");
        }
        return new String(fileNameBytes, 0, fileNameLength).trim();
    }
}
