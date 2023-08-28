package pers.fulsun.cleanpicfxml.handle;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.fulsun.cleanpicfxml.controller.MainPageController;
import pers.fulsun.cleanpicfxml.utils.FileSearchUtil;
import pers.fulsun.cleanpicfxml.utils.FileUtil;
import pers.fulsun.cleanpicfxml.utils.ImageUtil;
import pers.fulsun.cleanpicfxml.utils.MD5Util;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownHandle {
    // 正则表达式模式，匹配Markdown图片链接
    private static final String IMAGEP_REG = "!\\[([^\\]]*)\\]\\(([^\\)]+)\\s?\"([^\"]*)\"\\)|!\\[([^\\]]*)\\]\\(([^\\)]+)\\)";
    // 创建Pattern对象
    private static final Pattern IMAGE_PATTERN = Pattern.compile(IMAGEP_REG);
    private static final Logger logger = LoggerFactory.getLogger(MarkdownHandle.class);


    public static void handle(String scanMarkdownPath, boolean cleanflag, boolean downloadflag) throws IOException, NoSuchAlgorithmException {
        List<File> files = new ArrayList<>();
        File processDirectory = new File(scanMarkdownPath);
        recursivelyCollectMarkdownFiles(processDirectory, files);
        if (files.size() == 0) {
             logger.info("处理完成：未找到 MarkDown 文档。");
        } else {
             logger.info("找到文档 " + files.size() + " 篇文档，开始清理文档中的无用图片。");
            handlePicture(files, cleanflag, downloadflag);
        }
    }

    /**
     * 处理图片
     *
     * @param fileList             处理的文档列表
     * @param cleanUselessPicture  是否清理无效图片
     * @param downloadImageToLocal 是否将图片下载到本地
     */
    public static void handlePicture(List<File> fileList, boolean cleanUselessPicture, boolean downloadImageToLocal) throws IOException, NoSuchAlgorithmException {
        int fileIndex = 0;
        for (File markdownfile : fileList) {
            fileIndex++;
             logger.info("开始处理第【" + fileIndex + "】个文件， " + markdownfile.getCanonicalFile());
            deleteUselessPicture(markdownfile, cleanUselessPicture, downloadImageToLocal);
            System.out.print("\n");
        }
    }

    /**
     * 清理失效的图片
     *
     * @param markdownfile         markdown文件路径
     * @param cleanUselessPicture  是否清理无效图片
     * @param downloadImageToLocal 是否将图片下载到本地
     * @throws IOException
     */
    private static void deleteUselessPicture(File markdownfile, boolean cleanUselessPicture, boolean downloadImageToLocal) throws IOException, NoSuchAlgorithmException {
        if (!cleanUselessPicture) {
            return;
        }
        String markdownContent = FileUtil.readFileConten(markdownfile);
        // 创建Matcher对象
        Matcher matcher = IMAGE_PATTERN.matcher(markdownContent);
        // 查找匹配的图片链接
        List<String> usingPicList = new ArrayList();
        boolean contentChangeFlag = false;
        while (matcher.find()) {
            // 图片的URL
            String imageUrl = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(5).trim();
            String localImageAddress = downloadImageToLocal(imageUrl, markdownfile.getAbsolutePath(), downloadImageToLocal);
            if (localImageAddress != null) {
                int start = localImageAddress.lastIndexOf(File.separator);// 替换Markdown中的图片链接为本地路径
                String imageFileName = localImageAddress.substring(start + 1);
                // 文件存放在markdown同名目录下
                String imageRelativePath = FileUtil.getFileNameWithoutExtension(markdownfile.getAbsolutePath()) + File.separator + imageFileName;
                if (!imageUrl.endsWith(imageRelativePath)) {
                    contentChangeFlag = true;
                    markdownContent = markdownContent.replace(imageUrl, imageRelativePath);
                }
                usingPicList.add(imageFileName);
            }
        }
        if (contentChangeFlag) {
            FileUtil.saveTextToFile(markdownContent, markdownfile);
        }
        // 清理图片
        deleteUselessPicture(usingPicList, markdownfile.getAbsolutePath().substring(0, markdownfile.getAbsolutePath().indexOf(".md")));
    }

    private static void deleteUselessPicture(List<String> usingPicList, String picPath) {
        // 创建失效文件夹路径
        String invalidFolderName = "失效文件";
        String invalidPath = picPath + File.separator + invalidFolderName;
        FileUtil.createDirectory(invalidPath);
        File invalidFolder = new File(invalidPath);
        // 获取picPath文件夹中的所有文件
        File picFolder = new File(picPath);
        File[] picContents = picFolder.listFiles();
        // 遍历所有文件和文件夹，检查是否需要保留
        boolean invalidImageFlag = false;
        for (File pic : picContents) {
            if (pic.isDirectory()) {
                if (!invalidFolderName.equals(pic.getName())) {
                    FileUtil.moveFileOrFolder(pic, invalidFolder);
                    invalidImageFlag = true;
                }
            } else if (pic.isFile()) {
                String contentName = pic.getName();
                if (!usingPicList.contains(contentName)) {
                    FileUtil.moveFileOrFolder(pic, invalidFolder);
                    invalidImageFlag = true;
                }
            }
        }
        if (!invalidImageFlag) {
            invalidFolder.delete();
        } else {
             logger.info("已将未使用的图片移动到" + invalidFolder.getAbsolutePath());
        }
    }

    /**
     * 根据url链接下载文件到与markdown同名目录下
     *
     * @param imageUrl             图片链接
     * @param markdownfile         markdown文件路径
     * @param downloadImageToLocal
     * @return 图片的文件名和后缀
     */
    private static String downloadImageToLocal(String imageUrl, String markdownfile, boolean downloadImageToLocal) throws IOException, NoSuchAlgorithmException {
        String localImagePath = null;
        String parentDir = new File(markdownfile).getParent();
        File downloadPath = new File(parentDir, FileUtil.getFileNameWithoutExtension(markdownfile));

        int imageType = ImageUtil.isImageUrl(imageUrl);
        if (downloadImageToLocal && imageType > 0) {
            if (imageType == 1) {
                return ImageUtil.downloadImageByURL(imageUrl, downloadPath, imageUrl.substring(imageUrl.lastIndexOf("/") + 1));
            }
            if (imageType == 2) {
                return ImageUtil.coverImageToFile(imageUrl, downloadPath);
            }
        }
        // 处理本地图片
        if (imageType == 0) {
            // 获取markdown同名文件夹下的
            File imagePath = new File(parentDir, imageUrl);
            if (imagePath.exists()) {
                localImagePath = imagePath.getAbsolutePath();
            } else {
                 logger.info("未在Markdown同名目录下找到图片: " + imageUrl);
                return null;
            }
            if (localImagePath != null) {
                String md5 = MD5Util.calculateMD5ByFileContent(localImagePath);
                String fileExtension = ImageUtil.getFileExtension(localImagePath);
                // 将 MD5 值作为文件名
                String newFileName = md5 + fileExtension;
                // 重命名文件
                File localImageFile = new File(localImagePath);
                File newFile = new File(localImageFile.getParent(), newFileName);
                localImageFile.renameTo(newFile);
                return newFile.getAbsolutePath();
            }
        }
        return null;
    }

    public static void deleteUselessPicture(List<File> mdFileList) throws IOException, NoSuchAlgorithmException {
        int fileIndex = 0;

        for (File file : mdFileList) {
            fileIndex++;
             logger.info("Processing file #" + fileIndex + ": " + file.getAbsolutePath());
            String markdownContent = FileUtil.readFileConten(file);
            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
//             logger.info("--------------------typora无效图片删除程序--------------------");
//             logger.info("检测文件如下：" + file);
//             logger.info("检测目录       ：" + file.getParent());
//             logger.info("*********************【md 图片文件存放路径】*********************");
            // 创建Matcher对象
            Matcher matcher = IMAGE_PATTERN.matcher(markdownContent);
            boolean isChange = false;
            // 查找匹配的图片链接
            while (matcher.find()) {
                // String altText = matcher.group(1) != null ? matcher.group(1).trim() : matcher.group(4).trim(); // 图片的alt文本
                String imageUrl = matcher.group(2) != null ? matcher.group(2).trim() : matcher.group(5).trim(); // 图片的URL
                 logger.info("Image URL: " + imageUrl);

                try {
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        // 获取图片文件名
                        String imageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        // 构建目标文件路径
                        String targetFilePath = file.getParent() + File.separator + fileName + File.separator + imageFileName;
                        File downloadImage = new File(targetFilePath);
                        if (!downloadImage.exists()) {
                            // 创建目标文件所在的目录（如果不存在）
                            FileUtil.createDirectory(file.getParent() + File.separator + fileName);
                            // 下载图片
//                            ImageUtil.downloadImageByURL(imageUrl, targetFilePath);
                            // 计算文件的 MD5 值
                            String md5 = MD5Util.calculateMD5ByFileContent(targetFilePath);
                            String fileExtension = ImageUtil.getFileExtension(targetFilePath);
                            // 将 MD5 值作为文件名
                            String newFileName = md5 + fileExtension;
                            // 重命名文件
                            File newFile = new File(downloadImage.getParent(), newFileName);
                            downloadImage.renameTo(newFile);
                        }
                         logger.info("替换Markdown" + fileName + "中的图片链接为本地路径" + imageUrl);
                        // 替换Markdown中的图片链接为本地路径
                        markdownContent = markdownContent.replace(imageUrl, fileName + "/" + imageFileName);
                        isChange = true;
                    } else {
                        // 重命名本地图片
                        String targetFilePath;
                        if (imageUrl.startsWith("data:image")) {
                            targetFilePath = ImageUtil.base64ToImage(imageUrl, file.getParent() + File.separator + fileName);
                        } else {
                            targetFilePath = file.getParent() + File.separator + imageUrl.replace("/", File.separator);
                        }
                        assert targetFilePath != null;
                        File imagefile = new File(targetFilePath);
                        if (!imagefile.exists()) {
                            String imageName = imageUrl.substring(imageUrl.indexOf("/") + 1);
                            String testurl = "http://127.0.0.1:9000/typora/" + imageName;

                            // 从本地获取
                            int lastDotIndex = imageName.lastIndexOf('.');
                            if (lastDotIndex > 0) {
                                targetFilePath = FileSearchUtil.searchForImage(imageName.substring(0, lastDotIndex));
                            }
                            if (targetFilePath == null || !new File(targetFilePath).exists()) {
                                 logger.info(imageUrl + " 找不到了");
                                break;
                            }
                        }
                        // 计算文件的 MD5 值
                        String md5 = MD5Util.calculateMD5ByFileContent(targetFilePath);
                        String fileExtension = ImageUtil.getFileExtension(targetFilePath);
                        // 将 MD5 值作为文件名
                        String newFileName = md5 + fileExtension;
                        if (!newFileName.equals(imageUrl.substring(imageUrl.indexOf("/") + 1))) {
                            // 重命名文件
                            File newFile = new File(imagefile.getParent(), newFileName);
                            imagefile.renameTo(newFile);
                            // 替换Markdown中的图片链接为本地路径
                            markdownContent = markdownContent.replace(imageUrl, fileName + "/" + newFileName);
                            isChange = true;
                        }

                    }
                } catch (IOException e) {
                    System.err.println("无法下载图片: " + imageUrl);
                    e.printStackTrace();
                }

            }
            if (isChange) {
                // 保存更新后的Markdown文件
                FileUtil.saveTextToFile(markdownContent, file.getAbsoluteFile());
            }
        }
    }

    /**
     * 递归查找Markdown文档
     *
     * @param path     查找路径
     * @param fileList 返回的文档列表
     */
    public static void recursivelyCollectMarkdownFiles(File path, List<File> fileList) {
        File[] files = path.listFiles();
        for (File file : files != null ? files : new File[0]) {
            if (file.isDirectory()) {
                recursivelyCollectMarkdownFiles(file, fileList);
            } else if (file.isFile() && file.getName().endsWith(".md")) {
                fileList.add(file);
            }
        }
    }

}
