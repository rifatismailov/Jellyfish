package org.example.communication;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.Jellyfish;


import java.io.InputStream;

public class Reader {

    private static final Logger LOGGER = LogManager.getLogger(Jellyfish .class);

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
