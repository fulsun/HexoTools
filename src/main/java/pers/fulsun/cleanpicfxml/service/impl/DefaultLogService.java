package pers.fulsun.cleanpicfxml.service.impl;


import pers.fulsun.cleanpicfxml.service.LogService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DefaultLogService  implements LogService {
    private final BufferedWriter errorLog;

    public DefaultLogService(String logFile) throws IOException {
        this.errorLog = Files.newBufferedWriter(Paths.get(logFile),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void logError(String message) {
        System.err.println(message);
        try {
            errorLog.write(message);
            errorLog.newLine();
            errorLog.flush();
        } catch (IOException e) {
            System.err.println("写入日志失败: " + e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        if (errorLog != null) {
            errorLog.close();
        }
    }
}
