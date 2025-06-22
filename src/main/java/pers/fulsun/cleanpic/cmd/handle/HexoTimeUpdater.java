package pers.fulsun.cleanpic.cmd.handle;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexoTimeUpdater {
    // 支持的日期时间格式
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd",
            "EEE MMM dd HH:mm:ss zzz yyyy" // 类似 "Sun Sep 02 21:00:00 CST 2018"
    };

    // Hexo日期格式（用于写入YAML）
    private static final DateTimeFormatter HEXO_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static void processDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.md")) {
            for (Path file : stream) {
                updateFileTimestamps(file);
            }
        }

        // 递归处理子目录
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    processDirectory(entry);
                }
            }
        }
    }

    private static void updateFileTimestamps(Path filePath) {
        try {
            String content = Files.readString(filePath);
            FrontMatterInfo fmInfo = parseFrontMatter(content);

            if (fmInfo == null) {
                System.out.println("➡️ 跳过无有效 Front Matter 的文件: " + filePath.getFileName());
                return;
            }

            // 保存原始文件创建时间（用于没有date字段时）
            FileTime originalCreationTime = Files.readAttributes(filePath, BasicFileAttributes.class)
                    .creationTime();

            ZonedDateTime dateTime = fmInfo.date;
            ZonedDateTime updatedTime = fmInfo.updated != null ? fmInfo.updated : dateTime;

            boolean dateAdded = false;

            // 如果没有找到有效的date字段，使用文件原始创建时间补充
            if (dateTime == null) {
                System.out.print("⚠️ 没有找到有效的日期字段");

                // 创建时间可用时使用
                if (originalCreationTime != null) {
                    dateTime = originalCreationTime.toInstant().atZone(ZoneId.systemDefault());
                    System.out.printf("，使用文件创建时间补充: %s | ",
                            dateTime.format(HEXO_DATE_FORMATTER));

                    // 添加date字段到YAML front matter
                    fmInfo.yamlContent = addDateToYaml(fmInfo.yamlContent, dateTime);
                    dateAdded = true;
                } else {
                    System.out.println("，且无法获取文件创建时间，跳过: " + filePath.getFileName());
                    return;
                }
            }

            ZonedDateTime finalDateTime = dateTime;
            ZonedDateTime finalUpdatedTime = updatedTime;

            // 更新文件内容（如果需要添加date字段）
            if (dateAdded) {
                String newContent = String.format("---\n%s\n---\n%s",
                        fmInfo.yamlContent,
                        fmInfo.postContent);
                Files.writeString(filePath, newContent, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("已更新文件内容添加date字段");
            }

            // 设置文件时间戳
            setFileTimestamps(filePath, finalDateTime, finalUpdatedTime);

            System.out.printf("✅ %s | 创建: %s | 修改: %s%n",
                    filePath.getFileName(),
                    finalDateTime.format(HEXO_DATE_FORMATTER),
                    finalUpdatedTime.format(HEXO_DATE_FORMATTER));

        } catch (Exception e) {
            System.err.println("处理文件出错: " + filePath);
            e.printStackTrace();
        }
    }

    private static String addDateToYaml(String yamlContent, ZonedDateTime dateTime) {
        // 添加新行（如果内容不为空）
        if (!yamlContent.isEmpty() && !yamlContent.endsWith("\n")) {
            yamlContent += "\n";
        }
        return yamlContent + "date: \"" + dateTime.format(HEXO_DATE_FORMATTER) + "\"";
    }

    private static FrontMatterInfo parseFrontMatter(String content) {
        // 匹配 YAML front matter（介于 --- 之间的部分）
        Pattern pattern = Pattern.compile("^---\\s*\\n(.*?)\\n---\\s*\\n(.*)$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
            return null;
        }

        String yamlContent = matcher.group(1);
        String postContent = matcher.group(2);

        FrontMatterInfo fmInfo = new FrontMatterInfo();
        fmInfo.yamlContent = yamlContent;
        fmInfo.postContent = postContent;

        // 简单解析 YAML
        String[] lines = yamlContent.split("\\n");
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("\"", "");

                    switch (key) {
                        case "date":
                            fmInfo.date = parseDateTime(value);
                            break;
                        case "updated":
                            fmInfo.updated = parseDateTime(value);
                            break;
                    }
                }
            }
        }

        return fmInfo;
    }

    private static ZonedDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // 清理字符串中的引号
        dateString = dateString.replaceAll("['\"]", "").trim();

        // 尝试解析为ISO格式（带时区）
        try {
            return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (DateTimeParseException e1) {
            // 忽略，继续尝试其他格式
        }

        // 尝试解析为常见的格式
        for (String format : DATE_FORMATS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return ZonedDateTime.parse(dateString, formatter.withZone(ZoneId.systemDefault()));
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        // 尝试解析为Instant（例如，时间戳字符串）
        try {
            long epochMilli = Long.parseLong(dateString);
            return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
        } catch (NumberFormatException e) {
            // 不是数字
        }

        System.err.println("无法解析日期字符串: " + dateString);
        return null;
    }

    private static void setFileTimestamps(Path filePath, ZonedDateTime creation, ZonedDateTime modification) {
        try {
            // 设置文件修改时间
            Files.setLastModifiedTime(filePath, FileTime.from(modification.toInstant()));

            // 设置文件创建时间（在支持的平台上）
            try {
                BasicFileAttributeView attributes = Files.getFileAttributeView(filePath,
                        BasicFileAttributeView.class);
                attributes.setTimes(FileTime.from(modification.toInstant()),
                        null, // 不修改访问时间
                        FileTime.from(creation.toInstant()));
            } catch (UnsupportedOperationException e) {
                // 如果不支持设置创建时间，则忽略
            }
        } catch (IOException e) {
            System.err.println("无法设置文件时间戳: " + filePath);
        }
    }

    static class FrontMatterInfo {
        String yamlContent;
        String postContent;
        ZonedDateTime date;
        ZonedDateTime updated;
    }
}