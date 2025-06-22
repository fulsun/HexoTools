package pers.fulsun.cleanpic.cmd.handle;

import pers.fulsun.cleanpic.cmd.utils.Md5Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ImageRenamer {
    public Map<String, List<String>> generateRenameMap(String postsDirectory) {
        Map<String, List<String>> imageRenameMap = new HashMap<>();
        System.out.println(">>>>> 开始进行图片重命名");
        MarkdownImageChecker markdownImageChecker = new MarkdownImageChecker();
        Set<File> allMarkdownFiles = markdownImageChecker.getAllMarkdownFiles(Path.of(postsDirectory));
        Optional.of(allMarkdownFiles).ifPresent(files -> files.forEach(file -> {
            try {
                Set<String> imageUrls = markdownImageChecker.extractImagePathsFromMarkdown(file);
                imageUrls.forEach(imageUrl -> {
                    // 获取图片
                    Path imagePath = Path.of(file.getParent()).resolve(imageUrl);
                    if (Files.exists(imagePath)) {
                        // 获取MD5
                        try {
                            String md5Hash = Md5Utils.calculateMD5(imagePath);
                            String oldImageName = imagePath.getFileName().toString().substring(0, imagePath.getFileName().toString().lastIndexOf("."));
                            if (!oldImageName.equals(md5Hash)) {
                                String newImageUrl = imageUrl.replace(oldImageName, md5Hash);
                                imageRenameMap.computeIfAbsent(file.toString(), k -> new ArrayList<>()).add(imageUrl + "=>" + newImageUrl);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("图片不存在  >>>> " + imagePath);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return imageRenameMap;
    }

    public void applyRename(Map<String, List<String>> imageRenameMap) {
        if (imageRenameMap.isEmpty()) {
            return;
        }
        // 处理图片名称
        imageRenameMap.keySet().forEach(file -> {
            // 打印修复结果
            System.out.println("重命名文档：" + file);
            try {
                // 读取文件
                Path filePath = Path.of(file);
                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                List<String> fixStringList = imageRenameMap.get(file);
                // 遍历所有需要替换的路径
                for (String fixString : fixStringList) {
                    if (fixString != null && !fixString.isBlank()) {
                        String[] split = fixString.split("=>");
                        if (split.length == 2) {
                            String oldUrl = split[0];
                            String newUrl = split[1];
                            // 重命名图片
                            Path oldImagePath = filePath.getParent().resolve(oldUrl);
                            Path newImagePath = filePath.getParent().resolve(newUrl);
                            Files.move(oldImagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
                            content = content.replace(oldUrl, newUrl); // 更新 content
                            System.out.println("\t图片名称：" + oldUrl + " => " + newUrl);
                        }
                    }
                }
                // 写出文件
                Files.writeString(filePath, content);
            } catch (IOException e) {
                System.err.println("更新文件 " + file + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}