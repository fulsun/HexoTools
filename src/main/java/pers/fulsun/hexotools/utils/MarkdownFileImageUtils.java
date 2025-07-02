package pers.fulsun.hexotools.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Markdown 文件图像操作工具类
 */
public class MarkdownFileImageUtils {

    private static void processImageOperations(String file, List<String> fixStringList, Operation operation) {
        if (fixStringList == null || fixStringList.isEmpty()) {
            return;
        }

        try {
            Path filePath = Path.of(file);
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            for (String fixString : fixStringList) {
                if (fixString != null && !fixString.isBlank()) {
                    String[] split = fixString.split("=>");
                    if (split.length == 2) {
                        String oldUrl = split[0];
                        String newUrl = split[1];

                        Path oldImagePath = filePath.getParent().resolve(oldUrl);
                        Path newImagePath = filePath.getParent().resolve(newUrl);

                        if (Files.exists(oldImagePath)) {
                            operation.perform(oldImagePath, newImagePath);
                        }

                        content = content.replace(oldUrl, newUrl);
                        System.out.println("\t" + operation.getDescription() + ": " + oldUrl + " => " + newUrl);
                    }
                }
            }

            Files.writeString(filePath, content);
        } catch (IOException e) {
            System.err.println("更新文件 " + file + " 失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface Operation {
        void perform(Path oldPath, Path newPath) throws IOException;

        default String getDescription() {
            return "操作";
        }
    }

    /**
     * 应用图像重命名操作到指定文件
     *
     * @param file          图像所属 Markdown 文件路径
     * @param fixStringList 替换规则列表，格式为 oldPath=>newPath
     */
    public static void applyImageRenames(String file, List<String> fixStringList) {
        processImageOperations(file, fixStringList, (oldPath, newPath) -> {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        });
    }

    /**
     * 应用图像复制操作到指定文件
     *
     * @param file          图像所属 Markdown 文件路径
     * @param fixStringList 替换规则列表，格式为 oldPath=>newPath
     */
    public static void applyImageCopies(String file, List<String> fixStringList) {
        processImageOperations(file, fixStringList, (oldPath, newPath) -> {
            Files.createDirectories(newPath.getParent());
            Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        });
    }
}
