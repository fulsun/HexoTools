package pers.fulsun.cleanpicfxml.service;

import java.io.IOException;

// 日志服务接口

public interface LogService {
    void logError(String message);
    void close() throws IOException;
}
