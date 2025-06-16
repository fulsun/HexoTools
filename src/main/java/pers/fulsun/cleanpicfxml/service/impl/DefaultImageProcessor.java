package pers.fulsun.cleanpicfxml.service.impl;

import pers.fulsun.cleanpicfxml.bean.ImageReference;
import pers.fulsun.cleanpicfxml.bean.ImageReport;
import pers.fulsun.cleanpicfxml.config.Configuration;
import pers.fulsun.cleanpicfxml.service.FileService;
import pers.fulsun.cleanpicfxml.service.ImageProcessor;
import pers.fulsun.cleanpicfxml.service.LogService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import pers.fulsun.cleanpicfxml.utils.MarkdownFormatter;
import pers.fulsun.cleanpicfxml.utils.MarkdownVisitor;
import pers.fulsun.cleanpicfxml.utils.MdUrlUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// 默认图片处理器实现

public class DefaultImageProcessor implements ImageProcessor {
    private final Configuration config;
    private final FileService fileService;
    private final LogService logService;
    private final Map<String, List<String>> fileFixMap = new HashMap<>();
    private Map<String, List<Path>> galleryImageMap;

    public DefaultImageProcessor(Configuration config, FileService fileService, LogService logService) {
        this.config = config;
        this.fileService = fileService;
        this.logService = logService;
        // 初始化图库索引
        if (config.getGalleryDir() != null) {
            try {
                this.galleryImageMap = buildGalleryImageMap(config.getGalleryDir());
            } catch (IOException e) {
                logService.logError("初始化图库索引失败: " + e.getMessage());
                this.galleryImageMap = new HashMap<>();
            }
        } else {
            this.galleryImageMap = new HashMap<>();
        }
    }

    @Override
    public List<ImageReport> process(Path mdFile) throws IOException {
        List<ImageReport> reports = new ArrayList<>();
        String content = fileService.readFileContent(mdFile);
        List<ImageReference> images = extractImages(content);

        for (ImageReference image : images) {
            ImageReport report = processSingleImage(mdFile, image);
            reports.add(report);
        }

        // 应用所有修复
        if (config.isAutoFix() && !fileFixMap.isEmpty()) {
            applyAllFixes();
        }

        return reports;
    }

    private ImageReport processSingleImage(Path mdFile, ImageReference image) {
        Path parentDir = mdFile.getParent();
        String normalizedUrl = MdUrlUtils.normalizeImagePath(image.getUrl());

        ImageReport report = new ImageReport(mdFile.getFileName().toString(), image.getUrl(), normalizedUrl);

        if (MdUrlUtils.isWebUrl(normalizedUrl)) {
            report.setType("网络图片");
            report.setStatus(MdUrlUtils.checkValidImage(parentDir, normalizedUrl) ? "有效" : "无效");
            if (report.getStatus().equals("无效")) {
                logService.logError("网络图片：" + mdFile.getFileName().toString() + "\t" + report.getOriginalUrl());
            }
        } else {
            report.setType("本地图片");
            Path imgPath = parentDir.resolve(normalizedUrl);
            report.setLocalPath(imgPath.toString());
            boolean isValid = MdUrlUtils.checkValidImage(parentDir, normalizedUrl);
            report.setStatus(isValid ? "有效" : "无效");

            // 如果图片无效且启用了自动修复，尝试从图库修复
            if (!isValid && config.isAutoFix() && !galleryImageMap.isEmpty()) {
                String fileName = imgPath.getFileName().toString();
                if (galleryImageMap.containsKey(fileName)) {
                    try {
                        // 创建与md文件同名的目录
                        String mdFileName = mdFile.getFileName().toString();
                        String dirName = mdFileName.substring(0, mdFileName.lastIndexOf('.'));
                        Path targetDir = parentDir.resolve(dirName);
                        Files.createDirectories(targetDir);

                        // 复制图片到目标目录
                        Path sourceImage = galleryImageMap.get(fileName).get(0); // 取第一个匹配的图片
                        Path targetImage = targetDir.resolve(fileName);
                        Files.copy(sourceImage, targetImage, StandardCopyOption.REPLACE_EXISTING);

                        // 更新报告信息
                        String newUrl = dirName + "/" + fileName;
                        report.setSuggestedUpdate(newUrl);
                        report.setStatus("已修复");
                        report.setNormalized("是");

                        // 记录需要修复的图片（包含alt文本）
                        fileFixMap.computeIfAbsent(mdFile.toString(), k -> new ArrayList<>())
                                .add(image.getAltText() + "⇒" + image.getUrl() + "⇒" + newUrl);

                        logService.logError("已从图库修复图片：" + fileName + " 到 " + targetDir);
                    } catch (IOException e) {
                        logService.logError("修复图片失败：" + fileName + " - " + e.getMessage());
                    }
                }
            }
        }

        // 打印无效日志
        if (report.getStatus().equals("无效")) {
            logService.logError("图片无效：" + mdFile.getFileName().toString() + "\t" + report.getOriginalUrl());
        }

        // 检测是否需要规范化相对路径的格式 ./
        if (image.getUrl().equals(normalizedUrl)) {
            report.setNormalized("否");
        } else {
            report.setNormalized("是");
            report.setSuggestedUpdate(normalizedUrl);
            // 记录需要规范化的图片（包含alt文本）
            if (config.isAutoFix()) {
                fileFixMap.computeIfAbsent(mdFile.toString(), k -> new ArrayList<>())
                        .add(image.getAltText() + "⇒" + image.getUrl() + "⇒" + normalizedUrl);
            }
        }

        return report;
    }

    private Map<String, List<Path>> buildGalleryImageMap(Path galleryDir) throws IOException {
        Map<String, List<Path>> imageMap = new HashMap<>();
        try (Stream<Path> paths = Files.walk(galleryDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> isImageFile(p.getFileName().toString()))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        imageMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(path);
                    });
        }
        return imageMap;
    }

    private boolean isImageFile(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") ||
                lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif") ||
                lowerFileName.endsWith(".bmp") || lowerFileName.endsWith(".webp");
    }

    private List<ImageReference> extractImages(String content) {
        // 图片提取逻辑
        Parser parser = Parser.builder().build();
        Node document = parser.parse(content);
        MarkdownVisitor visitor = new MarkdownVisitor();
        document.accept(visitor);
        return visitor.imageRefs;
    }

    private void applyAllFixes() throws IOException {
        // 如果启用文档格式化，应用格式化

        for (Map.Entry<String, List<String>> entry : fileFixMap.entrySet()) {
            Path mdFile = Paths.get(entry.getKey());
            String content = fileService.readFileContent(mdFile);


            //格式化
            if (config.isFormatDocuments()) {
                 content = MarkdownFormatter.fixImageLinks(content);
            }

            for (String fix : entry.getValue()) {
                String[] parts = fix.split("⇒");
                if (parts.length == 3) {
                    String altText = parts[0];
                    String oldUrl = parts[1];
                    String newUrl = parts[2];
                    String oldSyntax = MdUrlUtils.buildImageSyntax(altText, oldUrl);
                    String newSyntax = MdUrlUtils.buildImageSyntax(altText, newUrl);
                    content = content.replace(oldSyntax, newSyntax);
                }
            }

            fileService.writeFileContent(mdFile, content);


        }
        fileFixMap.clear();
    }
}
