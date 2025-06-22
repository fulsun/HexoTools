package pers.fulsun.cleanpic.cmd.utils;

import pers.fulsun.cleanpic.cmd.common.Constant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 图片修复
 */
public class MarkdownImageFix {

    public MarkdownImageFix() {

    }

    public Map<String, List<String>> fix(Map<String, Map<String, List<String>>> checkResult, String grallyDir) {
        if (checkResult.values().stream().anyMatch(item -> item.size() > 1)) {
            // 初始化图库索引
            ImageIndexer indexer = ImageIndexer.getInstance();
            indexer.buildIndex(grallyDir);
        }
        Map<String, List<String>> fixMap = new HashMap<>();
        // 处理失效的图片
        handleInvalidImages(checkResult, fixMap);

        // 更具fixmap修改markdown文件
        updateMarkdownFiles(fixMap);

        // 打印结果
        MarkdownImageChecker.printCheckResult(checkResult.get(Constant.INVALID_IMAGES), checkResult.get(Constant.INVALID_REMOTE_IMAGES), checkResult.get(Constant.USED_IMAGES));

        return fixMap;
    }

    private void updateMarkdownFiles(Map<String, List<String>> fixMap) {
        Set<String> files = fixMap.keySet();
        files.stream().forEach(file -> {
            // 打印修复结果
            System.out.println("修复文件：" + file);
            try {
                // 读取文件
                String content = Files.readString(Path.of(file), StandardCharsets.UTF_8);
                List<String> fixStringList = fixMap.get(file);
                // 遍历所有需要替换的路径
                for (String fixString : fixStringList) {
                    if (fixString != null && !fixString.isBlank()) {
                        String[] split = fixString.split("=>");
                        if (split.length == 2) {
                            String oldUrl = split[0];
                            String newUrl = split[1];
                            content = content.replace(oldUrl, newUrl); // 更新 content
                            System.out.println("\t图片更新：" + oldUrl + " => " + newUrl);
                        }
                    }
                }
                // 写出文件
                Files.write(Path.of(file), content.getBytes(StandardCharsets.UTF_8));

            } catch (IOException e) {
                System.err.println("更新文件 " + file + " 失败：" + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleInvalidImages(Map<String, Map<String, List<String>>> checkResult, Map<String, List<String>> fixMap) {
        Map<String, List<String>> invalidImages = checkResult.get(Constant.INVALID_IMAGES);
        Set<String> fileSet = invalidImages.keySet();
        for (String file : fileSet) {
            // 处理失效的图片
            Optional.ofNullable(invalidImages.get(file)).ifPresent(invalidImageList -> {
                List successList = new ArrayList<>();
                invalidImageList.stream().forEach(path -> {
                    ImageIndexer instance = ImageIndexer.getInstance();
                    String filename = path.replace("\\", "/");
                    if (filename.contains("/")) {
                        filename = path.substring(path.lastIndexOf("/") + 1);
                    }
                    String imagePath = instance.getImagePath(filename);
                    if (imagePath != null) {
                        Path serarchImagePath = Path.of(imagePath);
                        if (serarchImagePath.toFile().exists()) {
                            // 复制图片
                            try {
                                // 创建目录
                                Path parent = Files.createDirectories(Path.of(file.replace(".md", "")));
                                String mdfilename = Path.of(file).getFileName().toString().replace(".md", "");
                                Files.copy(serarchImagePath, parent.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                                fixMap.computeIfAbsent(file, v -> new ArrayList<>()).add(path + "=>" + mdfilename + "/" + filename);
                                successList.add(path);
                            } catch (IOException e) {
                                System.out.println("复制图片失败：" + filename);
                            }
                        }
                    } else {
                        // System.out.println("图库不存在图片：" + path);
                    }
                });
                invalidImageList.removeAll(successList);
            });


        }
    }

}
