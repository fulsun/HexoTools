package pers.fulsun.hexotools.utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageValidationUtils {


    public boolean isRemoteImageValid(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }


    public void checkImagesBySameMdNamePath(Set<String> allImages, File mdFile, Map<String, List<String>> invalidImages, Map<String, List<String>> invalidRemoteImages, Map<String, List<String>> useImages) throws IOException {
        String key = mdFile.getCanonicalPath();
        String parentPath = mdFile.getParentFile().getCanonicalPath();
        String mdName = mdFile.getName().replace(".md", "");
        allImages.stream().map(path -> path.replaceAll("^[\\./]+", "")).forEach(path -> {
            if (!path.startsWith(mdName)) {
                invalidImages.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
            } else if (isWebUrl(path)) {
                if (isRemoteImageValid(path)) {
                    useImages.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
                } else {
                    invalidRemoteImages.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
                }
            } else {
                Path useImagePath = Path.of(parentPath).resolve(path);
                if (!Files.exists(useImagePath)) {
                    invalidImages.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
                } else {
                    useImages.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
                }
            }
        });
    }

    private boolean isWebUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("//");
    }
}
