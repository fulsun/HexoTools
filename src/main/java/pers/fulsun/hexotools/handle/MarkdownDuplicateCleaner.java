package pers.fulsun.hexotools.handle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownDuplicateCleaner {
    // 哈希值, 文件路径
    static Map<String, List<Path>> contentHashToFiles = new HashMap<>();

    // 回收站
    private static Path TRASH_DIR;


    public static void removeDuplicate(String postsDirectory) throws IOException, NoSuchAlgorithmException {
        TRASH_DIR = Path.of(postsDirectory).resolve("duplicate-trash");
        List<Path> files;

        try (Stream<Path> stream = Files.walk(Path.of(postsDirectory))) {
            files = stream.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".md")).collect(Collectors.toList());
        }
        // 分批处理
        for (Path file : files) {
            String content = removeYamlFrontMatter(Files.readString(file));
            String hash = computeMD5Hash(content);
            contentHashToFiles.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
        }

        // 输出重复文件
        for (List<Path> duplicates : contentHashToFiles.values()) {
            if (duplicates.size() > 1) {
                System.out.println("重复文件:");
                duplicates.forEach(System.out::println);
                System.out.println("------");

                // 将重复文件列表写入日志文件：
                // Files.write(Paths.get("duplicates.log"), duplicates.toString().getBytes());

                // 规则1：优先保留文件名最短的
                List<Path> shortestNames = findShortestNamedFiles(duplicates);
                if (shortestNames.size() > 1) {
                    // 规则2：如果文件名长度相同，则保留修改时间最新的
                    Path latestFile = findLatestModifiedFile(shortestNames);
                    System.out.println("保留文件: " + latestFile);
                    deleteOtherFilesToTrash(duplicates, latestFile);

                } else {
                    System.out.println("保留文件: " + shortestNames.get(0));
                    deleteOtherFilesToTrash(duplicates, shortestNames.get(0));
                }
            }
        }

        // 清理哈希值映射表
        contentHashToFiles.clear();
        System.out.println("重复文件处理完成。");
    }


    // 辅助方法：找出修改时间最新的文件
    private static Path findLatestModifiedFile(List<Path> files) {
        return files.stream().max(Comparator.comparingLong(p -> {
            try {
                return Files.getLastModifiedTime(p).toInstant().toEpochMilli();
            } catch (IOException e) {
                return 0L;
            }
        })).orElse(files.get(0));
    }

    // 辅助方法：删除其他重复文件
    private static void deleteOtherFiles(List<Path> duplicates, Path fileToKeep) {
        duplicates.stream().filter(p -> !p.equals(fileToKeep)).forEach(p -> {
            try {
                Files.delete(p);
                System.out.println("已删除: " + p);
            } catch (IOException e) {
                System.err.println("删除失败: " + p);
            }
        });

    }

    /**
     * 将重复文件移动到回收站，保留目录结构
     */
    private static void deleteOtherFilesToTrash(List<Path> duplicates, Path fileToKeep) {
        duplicates.stream().filter(p -> !p.equals(fileToKeep)).forEach(p -> {
            try {
                moveToTrash(p);
                System.out.println("已移动到回收站: " + p);
            } catch (IOException e) {
                System.err.println("移动失败: " + p + " | 错误: " + e.getMessage());
            }
        });
    }

    /**
     * 移动文件到回收站，保留原始目录结构
     * 示例：
     * - 原始路径: /project/docs/subdir/file.md
     * - 回收站路径: /trash/project/docs/subdir/file.md
     */
    private static void moveToTrash(Path source) throws IOException {
        // 1. 计算目标路径（在回收站中保留相同结构）
        Path target = TRASH_DIR.resolve(source.subpath(1, source.getNameCount())); // 跳过盘符（如 C:）        Path target = TRASH_DIR.resolve(relativePath);

        // 2. 确保目标目录存在
        Files.createDirectories(target.getParent());

        // 3. 移动文件（如果目标已存在，则追加时间戳）
        if (Files.exists(target)) {
            String newName = target.getFileName() + "_" + Instant.now().toEpochMilli() + ".md";
            target = target.resolveSibling(newName);
        }
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    // 辅助方法：找出文件名最短的文件（可能有多个长度相同的）
    private static List<Path> findShortestNamedFiles(List<Path> files) {
        int minLength = files.stream().mapToInt(p -> p.getFileName().toString().length()).min().orElse(0);

        return files.stream().filter(p -> p.getFileName().toString().length() == minLength).collect(Collectors.toList());
    }

    /**
     * 移除 YAML Front Matter（--- 包裹的部分）
     */
    private static String removeYamlFrontMatter(String content) {
        // 更高效的方式：直接查找第一个和第二个 "---"
        int start = content.indexOf("---");
        if (start == -1) return content;

        int end = content.indexOf("---", start + 3);
        if (end == -1) return content;

        return content.substring(end + 3).trim();
    }

    /**
     * 计算内容的 MD5 哈希值（也可用 SHA-256）
     */
    private static String computeMD5Hash(String content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        // 先移除图片 - 使用更简单的正则表达式
        content = content.replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "");
        // 标准化内容（移除多余空格/换行）
        content = content.replaceAll("\\s+", "");
        // 移除图片
        // content = content.replaceAll("!\\[.*?\\]\\(((?:[^()]|(?:\\([^()]*\\)))*)\\)", "");
        byte[] hashBytes = md.digest(content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}

