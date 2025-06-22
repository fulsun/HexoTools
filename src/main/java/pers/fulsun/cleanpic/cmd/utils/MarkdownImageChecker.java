package pers.fulsun.cleanpic.cmd.utils;

import org.apache.commons.io.FileUtils;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import pers.fulsun.cleanpic.cmd.common.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MarkdownImageChecker {

    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[.*?]\\((.*?)\\)");

    /**
     * 使用 CommonMark 解析器解析 Markdown 中的图片（更准确）
     *
     * @param markdownFile Markdown 地址
     * @return 图片URL列表
     */
    public static Set<String> parseImagesWithParser(File markdownFile) throws IOException {
        Set<String> images = new HashSet<>();
        Parser parser = Parser.builder().build();
        String markdown = FileUtils.readFileToString(markdownFile, "UTF-8");
        Node document = parser.parse(markdown);

        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Image image) {
                images.add(image.getDestination());
                super.visit(image);
            }
        });

        return images;
    }

    public static void printCheckResult(Map<String, List<String>> invalidImages, Map<String, List<String>> invalidRemoteImages, Map<String, List<String>> useImages) {
        // 打印结果
        System.out.println("》》》》》 检查结果如下:");
        for (String key : invalidImages.keySet()) {
            if (invalidImages.get(key).size() == 0) {
                return;
            }
            System.out.println("文档: " + key);
            for (String imagePath : invalidImages.getOrDefault(key, Collections.emptyList())) {
                System.out.println("\t 失效的图片: " + imagePath);
            }
            for (String imagePath : invalidRemoteImages.getOrDefault(key, Collections.emptyList())) {
                System.out.println("\t 失效的网络图片: " + imagePath);
            }
        }
        System.out.println("检查完成，共有 " + invalidImages.values().stream().mapToInt(List::size).sum() + " 个无效图片");
        System.out.println("检查完成，共有 " + useImages.size() + " 个使用的图片");
    }

    public Set<String> extractImagePathsFromMarkdown(File markdownFile) throws IOException {
        Set<String> imagePaths = new HashSet<>();
        String content = Files.readString(markdownFile.toPath());

        Matcher matcher = IMAGE_PATTERN.matcher(content);
        while (matcher.find()) {
            String path = matcher.group(1).trim();
            imagePaths.add(path);
        }
        return imagePaths;
    }

    public Set<File> getAllMarkdownFiles(Path directory) {
        Set<File> markdownFiles = new HashSet<>();

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(Path::toFile)
                    .forEach(markdownFiles::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return markdownFiles;
    }

    /**
     * 检查markdown图片状态
     *
     * @param mdDir
     * @return
     */
    public Map<String, Map<String, List<String>>> check(Path mdDir) {

        // 存储无效图片 key：文件名 value：图片路径
        Map<String, List<String>> invalidImages = new HashMap<>();
        Map<String, List<String>> invalidRemoteImages = new HashMap<>();
        Map<String, List<String>> useImages = new HashMap<>();

        Set<File> allMarkdownFiles = getAllMarkdownFiles(mdDir);
        if (allMarkdownFiles.isEmpty()) {
            return null;
        }

        // 遍历文档
        allMarkdownFiles.stream().forEach(md -> {
            try {
                checkSingleMarkdown(md, invalidImages, invalidRemoteImages, useImages);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("检查文档失败: " + md.getAbsolutePath());
            }
        });

        return Map.of(Constant.INVALID_IMAGES, invalidImages, Constant.USED_IMAGES, useImages, Constant.INVALID_REMOTE_IMAGES, invalidRemoteImages);
    }

    private void checkSingleMarkdown(File md, Map<String, List<String>> invalidImages, Map<String, List<String>> invalidRemoteImages, Map<String, List<String>> useImages) throws IOException {
        // 提前所有的图片
        Set<String> allImages = parseImagesWithParser(md);
        if (allImages.isEmpty()) {
            return;
        }
        ImageValidationService validationService = new ImageValidationService();
        // 本地通过检查markdown同名路径下
        validationService.ckeckImagesBySameMdNamePath(allImages, md, invalidImages, invalidRemoteImages, useImages);
    }
}
