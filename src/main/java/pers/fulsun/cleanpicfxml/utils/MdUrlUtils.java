package pers.fulsun.cleanpicfxml.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class MdUrlUtils {

    /**
     * 统一去除开头的./，统一目录分隔符为/
     *
     * @param path
     * @return
     */
    public static String normalizeImagePath(String path) {

        return path.replaceAll("^\\./", "").replaceAll("^\\\\", "").replace('\\', '/');
    }


    public static boolean isWebUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("//");
    }

    private static boolean checkWebImage(String normalizedUrl) {
        try {
            URL url = new URL(normalizedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            return (code >= 200 && code < 400);
        } catch (Exception e) {
//            logError("网络图片验证失败: " + imageUrl + " - " + e.getMessage());
            return false;
        }
    }

    public static boolean checkValidImage(Path parentDir, String normalizedUrl) {
        if (isWebUrl(normalizedUrl)) {
            return checkWebImage(normalizedUrl);
        }
        return checkLocalImage(parentDir.resolve(normalizedUrl));
    }

    private static boolean checkLocalImage(Path imagePath) {
        return Files.exists(imagePath);
    }

    public static String buildImageSyntax(String altText, String url) {
        if (altText == null || altText.isEmpty() || altText.trim().equals("null")) {
            return "![](" + url + ")"; // 处理空 alt 文本
        }
        return "![" + altText + "](" + url + ")";
    }

}
