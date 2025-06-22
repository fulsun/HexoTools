package pers.fulsun.cleanpic.cmd.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 单例工具类：用于构建和管理图片名到路径的索引
 */
public class ImageIndexer {

    // 支持的图片扩展名
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif");
    // 单例实例
    private static final ImageIndexer INSTANCE = new ImageIndexer();
    // 图片名 -> 完整路径 的索引
    private final Map<String, String> imageIndex = new HashMap<>();

    // 私有构造器，防止外部实例化
    private ImageIndexer() {
    }

    /**
     * 获取单例实例
     */
    public static ImageIndexer getInstance() {
        return INSTANCE;
    }

    /**
     * 扫描指定目录并构建图片索引
     *
     * @param directoryPath 要扫描的目录路径
     */
    public void buildIndex(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            System.err.println("无效的目录路径");
            return;
        }

        Path dirPath = Paths.get(directoryPath);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            System.err.println("指定的路径不存在或不是一个有效的目录: " + directoryPath);
            return;
        }

        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.forEach(path -> {
                if (isImageFile(path.toFile())) {
                    String imageName = path.getFileName().toString();
                    imageIndex.put(imageName, path.toAbsolutePath().toString());
                }
            });
        } catch (IOException e) {
            System.err.println("扫描目录时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据图片名称获取其完整路径
     *
     * @param imageName 图片名称
     * @return 图片的完整路径，如果未找到则返回 null
     */
    public String getImagePath(String imageName) {
        return imageIndex.getOrDefault(imageName, null);
    }

    /**
     * 获取当前构建的图片索引（不可变视图）
     *
     * @return 图片名到路径的映射表
     */
    public Map<String, String> getIndex() {
        return Collections.unmodifiableMap(imageIndex);
    }

    /**
     * 判断给定的文件是否为图片文件
     *
     * @param file 文件对象
     * @return 是否为图片文件
     */
    private boolean isImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    /**
     * 清除当前索引
     */
    public void clearIndex() {
        imageIndex.clear();
    }
}