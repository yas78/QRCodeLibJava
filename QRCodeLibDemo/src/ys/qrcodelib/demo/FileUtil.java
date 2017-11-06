package ys.qrcodelib.demo;

import java.io.File;

public class FileUtil {
    public static String getFileNameWithoutExtension(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path");
        }

        File file = new File(path);
        String fileName = file.getName();

        int lastPosition = fileName.lastIndexOf('.');

        if (lastPosition > 0) {
            return fileName.substring(0, lastPosition);
        } else {
            return fileName;
        }
    }
}
