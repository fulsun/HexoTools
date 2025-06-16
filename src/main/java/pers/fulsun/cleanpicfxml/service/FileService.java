package pers.fulsun.cleanpicfxml.service;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// 文件操作服务接口
public interface FileService {
    List<Path> findMarkdownFiles(Path directory);
    String readFileContent(Path file) throws IOException;
    void writeFileContent(Path file, String content) throws IOException;
}