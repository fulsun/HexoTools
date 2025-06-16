package pers.fulsun.cleanpicfxml.service.impl;


import pers.fulsun.cleanpicfxml.service.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultFileService implements FileService {
    @Override
    public List<Path> findMarkdownFiles(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".md"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("查找Markdown文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String readFileContent(Path file) throws IOException {
        return new String(Files.readAllBytes(file));
    }

    @Override
    public void writeFileContent(Path file, String content) throws IOException {
            Files.write(file, content.getBytes());
    }
}
