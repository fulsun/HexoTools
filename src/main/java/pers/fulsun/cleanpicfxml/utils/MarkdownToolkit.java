package pers.fulsun.cleanpicfxml.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//package utils;
//
//import pers.fulsun.cleanpicfxml.service.bean.ImageReference;
//import org.commonmark.node.*;
//import org.commonmark.parser.Parser;
//import utils.MarkdownVisitor;
//
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.file.*;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;
//import java.util.stream.Stream;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
public class MarkdownToolkit {
//    private static final String ERROR_LOG_FILE = "error.log";
//    private static BufferedWriter errorLog;
//
//    // 初始化错误日志
//    private static void initErrorLog() throws IOException {
//        errorLog = Files.newBufferedWriter(Paths.get(ERROR_LOG_FILE), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//    }
//
//    // 关闭错误日志
//    private static void closeErrorLog() throws IOException {
//        if (errorLog != null) {
//            errorLog.close();
//        }
//    }
//
//    // 记录错误信息
//    private static void logError(String message) {
//        System.err.println(message);
//        try {
//            errorLog.write(message);
//            errorLog.newLine();
//            errorLog.flush();
//        } catch (IOException e) {
//            System.err.println("写入日志失败: " + e.getMessage());
//        }
//    }
//
//    // Markdown图片重命名
//    public static void renameMarkdownImages(Path mdFile) throws IOException {
//        String originalContent = new String(Files.readAllBytes(mdFile));
//        Parser parser = Parser.builder().build();
//        Node document = parser.parse(originalContent);
//        MarkdownVisitor visitor = new MarkdownVisitor(mdFile);
//        document.accept(visitor);
//
//        String updatedContent = originalContent;
//        for (ImageReference ref : visitor.imageRefs) {
//            try {
//                String md5 = getMD5(ref.url);
//                String newUrl = md5 + getFileExtension(ref.url);
//                String oldImageSyntax = buildImageSyntax(ref.altText, ref.url);
//                String newImageSyntax = buildImageSyntax(ref.altText, newUrl);
//                updatedContent = updatedContent.replace(oldImageSyntax, newImageSyntax);
//            } catch (NoSuchAlgorithmException e) {
//                logError("生成 MD5 失败: " + ref.url + " - " + e.getMessage());
//            }
//        }
//        Files.write(mdFile, updatedContent.getBytes());
//    }
//
//    private static String getMD5(String input) throws NoSuchAlgorithmException {
//        MessageDigest md = MessageDigest.getInstance("MD5");
//        byte[] messageDigest = md.digest(input.getBytes());
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : messageDigest) {
//            String hex = Integer.toHexString(0xFF & b);
//            if (hex.length() == 1) {
//                hexString.append('0');
//            }
//            hexString.append(hex);
//        }
//        return hexString.toString();
//    }
//
//    private static String getFileExtension(String url) {
//        int lastIndex = url.lastIndexOf('.');
//        return lastIndex != -1 ? url.substring(lastIndex) : "";
//    }
//
//    // 网络图片下载
//    public static void downloadWebImage(String imageUrl, Path savePath) {
//        try {
//            URL url = new URL(imageUrl);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            try (InputStream in = conn.getInputStream();
//                 OutputStream out = Files.newOutputStream(savePath)) {
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//            }
//        } catch (Exception e) {
//            logError("网络图片下载失败: " + imageUrl + " - " + e.getMessage());
//        }
//    }
//
    // 文档格式化（简单示例：去除多余空行）
    public static void formatMarkdownDocument(Path mdFile) throws IOException {
        List<String> lines = Files.readAllLines(mdFile);
        List<String> formattedLines = new ArrayList<>();
        boolean lastLineEmpty = false;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                if (!lastLineEmpty) {
                    formattedLines.add("");
                    lastLineEmpty = true;
                }
            } else {
                formattedLines.add(line);
                lastLineEmpty = false;
            }
        }
        Files.write(mdFile, formattedLines);
    }
//
//    private static String buildImageSyntax(String altText, String url) {
//        if (altText == null || altText.isEmpty()) {
//            return "![](" + url + ")";
//        }
//        return "![" + altText + "](" + url + ")";
//    }
}