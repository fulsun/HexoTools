package pers.fulsun.cleanpicfxml.config;

import java.nio.file.Path;

// 配置类 - 集中管理参数和路径
public class Configuration {
    private final boolean autoFix;
    private final boolean renameImages;
    private final boolean downloadWebImages;
    private final boolean formatDocuments;
    private final Path postsDir;
    private final String errorLogFile;
    private final Path galleryDir;

    public Configuration(boolean autoFix, boolean renameImages, boolean downloadWebImages, boolean formatDocuments, Path postsDir, String errorLogFile, Path galleryDir) {
        this.autoFix = autoFix;
        this.renameImages = renameImages;
        this.downloadWebImages = downloadWebImages;
        this.formatDocuments = formatDocuments;
        this.postsDir = postsDir;
        this.errorLogFile = errorLogFile;
        this.galleryDir = galleryDir;
    }

    // Getters
    public boolean isAutoFix() {
        return autoFix;
    }

    public boolean isRenameImages() {
        return renameImages;
    }

    public boolean isDownloadWebImages() {
        return downloadWebImages;
    }

    public boolean isFormatDocuments() {
        return formatDocuments;
    }

    public Path getPostsDir() {
        return postsDir;
    }

    public String getErrorLogFile() {
        return errorLogFile;
    }

    public Path getGalleryDir() {
        return galleryDir;
    }
}
