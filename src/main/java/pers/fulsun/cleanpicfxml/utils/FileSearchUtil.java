package pers.fulsun.cleanpicfxml.utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSearchUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileSearchUtil.class);

    static String directoryPath = "C:\\Users\\sfuli\\Desktop\\blog-stellar\\source";
    private static Map<String, String> fileCache = new HashMap<>();
    static {
        buildFileCache(directoryPath);
    }

    public static void buildFileCache(String directoryPath) {
        Collection<File> files = FileUtils.listFiles(new File(directoryPath), null, true);
        for (File file : files) {
            String fileName = file.getName();
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileName = fileName.substring(0, lastDotIndex);
            }
            fileCache.put(fileName, file.getAbsolutePath());
        }
    }

    public static String searchForImage(String fileNameToSearch) {
        return fileCache.get(fileNameToSearch);
    }

    public static void main(String[] args) {
        String fileNameToSearch = "cd03c6ce1a8561390914213327e3f54b.png";
        String filePath = searchForImage(fileNameToSearch);

        if (filePath != null) {
             logger.info("File found at: " + filePath);
        } else {
             logger.info("File not found");
        }
    }
}
