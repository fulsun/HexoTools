package pers.fulsun.cleanpic.cmd.handle;

import pers.fulsun.cleanpic.cmd.common.Constant;
import pers.fulsun.cleanpic.cmd.utils.ImageIndexer;
import pers.fulsun.cleanpic.cmd.utils.MarkdownFileImageUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * 图片修复
 */
public class MarkdownImageFix {


    public void fix(String postsDirectory) {
        System.out.print("请输入图库路径：");
        String galleryDirectory = new Scanner(System.in).nextLine().trim();

        // 指定默认路径
        if (galleryDirectory.isEmpty()) {
            galleryDirectory = "C:\\Users\\fulsun\\Pictures\\unused-images";
        }

        // 验证路径有效性
        try {
            if (!Files.exists(Path.of(galleryDirectory))) {
                System.out.println("图库路径不存在！");
                return;
            }
        } catch (InvalidPathException e) {
            System.out.println("图库路径无效：" + e.getMessage());
            return;
        }

        // 实现具体逻辑
        Map<String, Map<String, List<String>>> checkResult = new MarkdownImageChecker().check(Path.of(postsDirectory));
        if (checkResult == null || checkResult.isEmpty()) {
            return;
        }

        fix(checkResult, galleryDirectory);
    }


    /**
     * 修复函数，根据检查结果更新图库索引和Markdown文件中的图片链接
     *
     * @param checkResult 包含检查结果的映射，键为检查类型，值为具体检查结果的映射
     * @param grallyDir   图库目录路径，用于初始化图库索引
     */
    public void fix(Map<String, Map<String, List<String>>> checkResult, String grallyDir) {
        // 判断是否存在任意一个子map的大小大于1（用于决定是否初始化图库）
        if (hasSubMapWithSizeGreaterThanOne(checkResult)) {
            // 初始化图库索引
            ImageIndexer indexer = ImageIndexer.getInstance();
            indexer.buildIndex(grallyDir);
        }
        // 缓存get结果，避免多次调用
        Map<String, List<String>> invalidImages = checkResult.getOrDefault(Constant.INVALID_IMAGES, Collections.emptyMap());
        Map<String, List<String>> invalidRemoteImages = checkResult.getOrDefault(Constant.INVALID_REMOTE_IMAGES, Collections.emptyMap());
        Map<String, List<String>> usedImages = checkResult.getOrDefault(Constant.USED_IMAGES, Collections.emptyMap());


        // 创建一个映射用于存储待修复的图片信息
        Map<String, List<String>> fixMap = new HashMap<>();
        // 处理失效的图片
        handleInvalidImages(invalidImages, fixMap);
        // 根据fixmap修改markdown文件
        updateMarkdownFiles(fixMap);

        // 打印结果，防止传入null导致异常
        MarkdownImageChecker.printCheckResult(invalidImages, invalidRemoteImages, usedImages);
    }

    /**
     * 检查是否存在子Map，其大小大于1
     * 该方法用于判断给定的Map中是否存在任何一个子Map（值为Map）的大小超过1
     * 主要解决的问题是判断复杂数据结构中子元素的数量是否满足特定条件
     *
     * @param checkResult 待检查的Map，其值为Map<String, List<String>>
     * @return 如果存在子Map大小大于1，则返回true；否则返回false
     */
    private boolean hasSubMapWithSizeGreaterThanOne(Map<String, Map<String, List<String>>> checkResult) {
        // 判断checkResult不为空，且存在子Map大小大于1的情况
        return checkResult != null && checkResult.values().stream().filter(Objects::nonNull).anyMatch(item -> item.size() > 1);
    }


    private void updateMarkdownFiles(Map<String, List<String>> fixMap) {
        if (fixMap == null || fixMap.isEmpty()) {
            return;
        }

        fixMap.forEach((file, fixStringList) -> {
            System.out.println("更新文档：" + file);
            MarkdownFileImageUtils.applyImageCopies(file, fixStringList);
        });
    }

    private void handleInvalidImages(Map<String, List<String>> invalidImages, Map<String, List<String>> fixMap) {
        if (fixMap == null) {
            throw new IllegalArgumentException("fixMap 不能为空");
        }
        if (invalidImages == null) return;

        ImageIndexer imageIndexer = ImageIndexer.getInstance();

        for (String file : invalidImages.keySet()) {
            List<String> invalidImageList = invalidImages.get(file);
            if (invalidImageList == null || invalidImageList.isEmpty()) continue;
            invalidImageList.removeIf(path -> {
                Path imagePathObj = processImagePath(path, imageIndexer);
                if (imagePathObj == null || !imagePathObj.toFile().exists()) {
                    return false;
                }

                try {
                    String filename = imagePathObj.getFileName().toString();
                    Path targetDir = Files.createDirectories(Path.of(file.replace(".md", "")));
                    String mdfilename = Path.of(file).getFileName().toString().replace(".md", "");

                    Path targetPath = targetDir.resolve(filename);
                    Files.copy(imagePathObj, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    fixMap.computeIfAbsent(file, v -> new ArrayList<>()).add(path + "=>" + mdfilename + "/" + filename);
                    return true; // 表示该元素会被移除
                } catch (IOException e) {
                    System.err.println("复制图片失败：" + imagePathObj.getFileName());
                    return false;
                }
            });
        }
    }

    // 抽取路径处理逻辑，便于复用和测试
    private Path processImagePath(String path, ImageIndexer imageIndexer) {
        Path normalizedPath = Path.of(path.replace("\\", "/"));
        String filename = normalizedPath.getFileName().toString();
        String imagePath = imageIndexer.getImagePath(filename);
        return imagePath == null ? null : Path.of(imagePath);
    }


}